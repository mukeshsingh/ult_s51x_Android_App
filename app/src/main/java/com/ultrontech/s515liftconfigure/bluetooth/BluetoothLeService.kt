package com.ultrontech.s515liftconfigure.bluetooth

import android.Manifest
import android.R.attr.delay
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.HomeActivity
import com.ultrontech.s515liftconfigure.S515LiftConfigureApp
import com.ultrontech.s515liftconfigure.models.*
import kotlinx.coroutines.Job
import org.json.JSONObject
import java.util.*


class BluetoothLeService : Service() {
    private val mBinder = LocalBinder()
    private var mAddress: String? = null
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private var mScanning = false
    private val mHandler = Handler(Looper.getMainLooper())
    lateinit var mAccessAuthKey: String
    var device: Device? = null
    private var mSessionId: String? = null
    // Written from binder threads, the main thread, and the GSM worker thread.
    @Volatile
    private var mBusy = false
    var lifts = listOf<ScanDisplayItem>()

    var devices: HashMap<String, ScannedDevice> = HashMap<String, ScannedDevice> ()
    var updateCount: Int = 0

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager
        service = this
        Log.e(TAG, ">>>>>>>> BluetoothLeService initialized.")
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?
            mBluetoothAdapter = mBluetoothManager?.adapter
            mBluetoothLeScanner = null
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
                return false
            } else if (mBluetoothAdapter!!.bluetoothLeScanner == null) {
                return false;
            }
            mBluetoothAdapter?.isEnabled
            mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner
        }

        return true
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d(TAG, "Bluetooth has been OFF")
                        broadcastUpdate(ACTION_BLUETOOTH_OFF, null)
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> {}
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluetooth has been ON")
                        broadcastUpdate(ACTION_BLUETOOTH_ON, null)
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> {}
                }
            }
        }
    }

    fun refreshScannedList() {
        lifts = devices.map {
            ScanDisplayItem(
                it.value.peripheral.address,
                it.value.name,
                it.value.connected,
                it.value.modelNumber
            )
        }

        broadcastUpdate(ACTION_LIFT_LIST_UPDATED, null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.e(TAG, ">>>>>>>> BluetoothLeService onBind.")

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mReceiver, filter, RECEIVER_NOT_EXPORTED)

        return mBinder
    }

    override fun unbindService(conn: ServiceConnection) {
        unregisterReceiver(mReceiver)
        super.unbindService(conn)
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            Log.e(TAG, ">>>>>>>> BluetoothLeService LocalBinder getService.")
            return this@BluetoothLeService
        }
    }

    fun link(d: Device) {
        device = d

        updateConnectionState(true)

        broadcastUpdate(ACTION_CONNECTION_UPDATE, null)
    }

    fun updateConnectionState(isLink: Boolean) {
        val lift = device?.lift?.liftId?.let { find(it) }

//        if (isLink) {
//            if (lift?.connected == true) {
//                device?.connectionState = LiftConnectionState.connected_noauth
//            }
//        } else {
            if (lift?.connected == true && lift.authorised) {
                device?.connectionState = LiftConnectionState.connected_auth
            } else if (lift?.connected == true) {
                device?.connectionState = LiftConnectionState.connected_noauth
            } else if (lift?.connected == false) {
                device?.connectionState = LiftConnectionState.not_connected
            } else {
                device?.connectionState = LiftConnectionState.connect_error
            }
//        }
    }

    fun authorise(userLift : UserLift) {
        val lift = mAddress?.let { find(it) }
        if (lift != null) {
            Log.d(TAG, "Connect to Device : ${lift.name}")

            if (!lift.connected || lift.authControl == null) {
                return
            }

            with(S515LiftConfigureApp) {
                if (waitIdle(LiftBT.GATT_TIMEOUT)) {
                    val token =
                        if (profileStore.hasEngineerCapability) ProfileStore.EngineerTokenKey else userLift.accessKey.code()
                    val b1 = ((token shr 24) and 0xff).toByte()
                    val b2 = ((token shr 16) and 0xff).toByte()
                    val b3 = ((token shr 8) and 0xff).toByte()
                    val b4 = ((token and 0xff)).toByte()

                    val command: ByteArray =
                        byteArrayOf(S515BTCommand.btCmdAuthenticate.toByte(), 0x04, b1, b2, b3, b4)

                    lift.authControl?.value = command

                    val success = writeCharacteristic(lift.authControl!!, value = command)
                    Log.i(TAG, "======>>>>>> Characteristic written for modify auth: $success")
                }
            }
        }
    }

    fun find(address: String) : ScannedDevice? {
        return devices[address]
    }

    private lateinit var timer: Job
    fun connect(address: String?, authKey: String): Boolean {
        if (mBluetoothAdapter == null || address == null || !checkPermission()) {
             Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false
        }

        val connectionState = mBluetoothAdapter?.getProfileConnectionState(BluetoothProfile.GATT)
        val bluetoothDevice = find(address)
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED && bluetoothDevice != null) {
            // Previously connected device. Try to reconnect.
            if (mAddress != null && address == mAddress && mBluetoothGatt != null && !bluetoothDevice.connected) {
                // Log.d(TAG, "Re-use GATT connection");
                broadcastUpdate(ACTION_GATT_CONNECTING)

                return mBluetoothGatt!!.connect()
            }

            this.mAccessAuthKey = authKey
            return try {
                // We want to directly connect to the device, so we are setting the
                // autoConnect parameter to false.
                 Log.d(TAG, "Create a new GATT connection.")
                broadcastUpdate(ACTION_GATT_CONNECTING)
                mBluetoothGatt?.close()
                mBluetoothGatt = bluetoothDevice.peripheral.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
                mAddress = address

                timer = S515LiftConfigureApp.instance.startCoroutineTimer(delayMillis = 20000) {
                    Log.d(TAG, "timer called");
                    close()
                    broadcastUpdate(ACTION_GATT_CONNECTION_FAILURE)
                }
                true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                false
            }
        } else if (bluetoothDevice != null && mBluetoothGatt != null){
            Log.w(TAG, "Attempt to connect in state: $connectionState")
            Handler(Looper.getMainLooper()).post {
                mBluetoothGatt?.discoverServices()
            }

            return false
        } else {
            return false
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            val device = find(mAddress!!)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                timer.cancel()
                // successfully connected to the GATT Server

                device?.connected = true
                updateConnectionState(false)
                broadcastUpdate(ACTION_GATT_CONNECTED, null)

                if (device != null) {
                    if (waitIdle(LiftBT.GATT_TIMEOUT) && checkPermission() && !device.isServicesDiscovered) {
            //                    Handler(Looper.getMainLooper()).post {
            //                        mBluetoothGatt?.discoverServices()
            //                    }
                        Handler(Looper.getMainLooper()).postDelayed(Runnable {
                            mBluetoothGatt?.discoverServices()
                            device.isServicesDiscovered = true
                            isUpdatingServices = false
                        }, 1000)
                    }
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                close()

                device?.connected = false
                device?.authorised = false

                lifts = listOf()
                devices = HashMap<String, ScannedDevice> ()
                this@BluetoothLeService.device = null

                updateConnectionState(false)
                broadcastUpdate(ACTION_GATT_DISCONNECTED)

                scanLeDevice()
            }

            Log.w(TAG, "onConnectionStateChange Status: $status, New state: $newState")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            super.onReliableWriteCompleted(gatt, status)

            Log.d(TAG, ">>>>>>>>>>>>>>> onReliableWriteCompleted: $status")
        }
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.d(TAG, "onCharacteristicRead: $status: " + characteristic?.value?.let { String(it, Charsets.UTF_8) })
            publish(characteristic)
            mBusy = false
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            Log.d(TAG, "onCharacteristicWrite: " + characteristic?.value?.get(0) +
                    characteristic?.value?.get(1) + characteristic?.value?.get(2)
            )
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            publish(characteristic)
            mBusy = false
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?, status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)

            mBusy = false
            Log.d(TAG, "======= onDescriptorRead: ")
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?, status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)

            mBusy = false
            Log.d(TAG, "======= onDescriptorWrite: $status")
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            mBusy = false
            Log.d(TAG, "======= onMtuChanged: $status Mtu: $mtu")
        }
    }

    private var bytes: ByteArray? = null
    private fun publish(characteristic: BluetoothGattCharacteristic?) {
        if (LiftBT.authCharUUID == characteristic?.uuid) {
            Log.d(TAG, "Hello")
        }
        if (LiftBT.isChunkedResponse(characteristic?.uuid)) {
            val data = characteristic?.value
            if (data != null && data.count() > 7) {
                val key = (data[0].toInt() and 0xff) shl 8 or (data[1].toInt() and 0xff)
                val numberOfChunks = (data[2].toInt() and 0xff) shl 8 or (data[3].toInt() and 0xff)
                val currentChunkNumber =
                    (data[4].toInt() and 0xff) shl 8 or (data[5].toInt() and 0xff)
                val dataLength = data[6]

                Log.d(
                    TAG,
                    "key: $key, numberOfChunk: $numberOfChunks, currentChunkNumber: $currentChunkNumber, dataLength: $dataLength"
                )

                bytes = if (bytes == null) {
                    data.copyOfRange(7, data.count())
                } else {
                    bytes!! + data.copyOfRange(7, data.count())
                }

                if (numberOfChunks == currentChunkNumber) {
//                    when (characteristic.uuid) {
//                        LiftBT.controlCharacteristic.uuid -> validateControlData(bytes)
//                    }

                    bytes = null
                }
            }
        } else {
            with(characteristic) {
                val data = this?.value
                String(data!!, Charsets.UTF_8)

                Handler(Looper.getMainLooper()).post {
                    when (this?.uuid) {
                        LiftBT.authCharUUID -> devices[mAddress]?.let { processAuth(data, it) }
                        LiftBT.levelsCharUUID -> processLevel(data)
                        LiftBT.infoCharUUID -> processInfo(data)
                        LiftBT.phoneCharUUID -> processPhone(data)
                        LiftBT.phoneConfigCharUUID -> processPhoneConfig(data)
                        LiftBT.jobCharUUID -> processJob(data)
                        LiftBT.wifiCharUUID -> processWifiDetail(data)
                        LiftBT.ssidsCharUUID -> processSSIDList(data)
                        LiftBT.gsmCharUUID -> processGsmDetail(data)

                        // Note for the chain below: mBusy was already cleared by
                        // onCharacteristicRead before this handler was posted. Clearing it
                        // again here clobbered the busy flag of any operation issued in
                        // between (auth write, CCCD writes, GSM read), making the next
                        // chained read collide and silently drop - the firmware revision
                        // then never arrived.
                        LiftBT.modelNumberCharUUID -> {
                            val cx = data.decodeToString()
                            print("[BT($mAddress)::Char($uuid))] : value updated -> [$cx)]")
                            devices[mAddress]?.modelNumber = cx
                            refreshScannedList()
                            val service = mBluetoothGatt?.getService(LiftBT.deviceControlServiceUUID)
                            val char = service?.getCharacteristic(LiftBT.manufacturerNameCharUUID)
                            readCharacteristicWithRetry(char)
                        }

                        LiftBT.manufacturerNameCharUUID -> {
                            val cx = data.decodeToString()
                            print("[BT($mAddress)::Char($uuid))] : value updated -> [$cx)]")
                            devices[mAddress]?.manufacturerName = cx
                            refreshScannedList()
                            val service =
                                mBluetoothGatt?.getService(LiftBT.deviceControlServiceUUID)
                            val char = service?.getCharacteristic(LiftBT.firmwareRevisionCharUUID)
                            readCharacteristicWithRetry(char)
                        }

                        LiftBT.firmwareRevisionCharUUID -> {
                            val cx = data.decodeToString()
                            print("[BT($mAddress)::Char($uuid))] : value updated for firmwareRevision -> [$cx)]")
                            devices[mAddress]?.firmwareRevision = cx
                            refreshScannedList()
                        }

                        else -> {
                            Log.d(TAG, "unknown characteristic")
                        }
                    }
                }
            }
        }
    }

    /**
     * readCharacteristic returns false when another GATT operation is in flight (Android
     * allows only one) - retrying instead of dropping the read keeps response-driven chains
     * like modelNumber -> manufacturer -> firmwareRevision alive. Each attempt is paced by
     * readCharacteristic's internal waitIdle.
     */
    private fun readCharacteristicWithRetry(characteristic: BluetoothGattCharacteristic?, attempts: Int = 5): Boolean {
        if (characteristic == null) return false
        for (attempt in 1..attempts) {
            if (readCharacteristic(characteristic) == true) return true
            Log.w(TAG, "read attempt $attempt failed for ${characteristic.uuid}, gatt busy")
            // The native stack rejected the read while busy with an op of its own (e.g. a
            // descriptor write) - give it time to complete before retrying.
            try { Thread.sleep(150) } catch (_: InterruptedException) {}
        }
        return false
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean? {
        Log.d(TAG, "readCharacteristic check Gatt: " + checkGatt())
//        if (!checkGatt()) return false
        if (waitIdle(LiftBT.GATT_TIMEOUT)) {
            mBusy = true
            val issued = checkPermission() && mBluetoothGatt?.readCharacteristic(characteristic) == true
            if (!issued) {
                // No callback will ever arrive for a rejected read - leaving mBusy set
                // stalls every later operation until an unrelated notify clears it.
                mBusy = false
            }
            return issued
        }

        return false
    }

    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray): Int? {
        if (!checkGatt()) return 0
        if (waitIdle(LiftBT.GATT_TIMEOUT)) {
            mBusy = true
            if (checkPermission()) {
                characteristic.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val status = mBluetoothGatt?.writeCharacteristic(
                                it,
                                value,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            )
                        if (status != BluetoothStatusCodes.SUCCESS) mBusy = false
                        return status
                    } else {
                        val issued = mBluetoothGatt?.writeCharacteristic(it) == true
                        if (!issued) mBusy = false
                        return if (issued) 0 else 1
                    }
                }
            }
            mBusy = false
        }
        return -1
    }

    fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true

        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun scanLeDevice() {
        devices = HashMap<String, ScannedDevice> ()
        refreshScannedList()

        if (!mScanning) { // Stops scanning after a pre-defined scan period.
            Log.d(TAG, ">>>>>>>>>>>>>>> scanLeDevice")
            mHandler.postDelayed({
                mScanning = false
                if (checkPermission()) stopScan()
//                if (devices.size == 0) {
//                    scanLeDevice()
//                }
            }, 4000)
            Log.d(TAG, ">>>>>>>>>>>>>>> scanLeDevice: $mScanning")

            mScanning = true
            broadcastUpdate(ACTION_BLUETOOTH_DEVICE_SCANNING)
            mBluetoothLeScanner?.startScan(leScanCallback)
        }
    }

    private fun stopScan() {
        if (checkPermission()) {
            mHandler.removeCallbacksAndMessages(null)
            mBluetoothLeScanner?.stopScan(leScanCallback)
            mScanning = false
            broadcastUpdate(ACTION_BLUETOOTH_DEVICE_SCANNING_STOPPED)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (checkPermission()) {
                if ((result.device.name ?: "").uppercase(Locale.ROOT).startsWith("SAVARIA")) {
                    Log.d(
                        TAG,
                        "========> Device found: " + result.device.address + " " + result.device.name
                    )

                    devices[result.device.address] = ScannedDevice(
                        result.device.address,
                        result.device.name,
                        connected = false,
                        ignore = false,
                        result.device
                    )

                    refreshScannedList()
                    broadcastUpdate(ACTION_BLUETOOTH_DEVICE_FOUND)
                }
            }
        }

        override fun onBatchScanResults(results: List<ScanResult?>?) {
            super.onBatchScanResults(results)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            broadcastUpdate(ACTION_GATT_CONNECTION_FAILURE)
        }
    }

    var isUpdatingServices = false;

    fun updateServices(isEnabled: Boolean) {
        if (isUpdatingServices) return
        isUpdatingServices = true
        try {
            updateServicesLocked(isEnabled)
        } finally {
            // Without the finally, an exception left the flag set forever and every later
            // service-discovery pass returned immediately, never linking characteristics.
            isUpdatingServices = false
        }
    }

    private fun updateServicesLocked(isEnabled: Boolean) {
        val services = mBluetoothGatt?.services

        if (waitIdle(LiftBT.GATT_TIMEOUT)) setMtu()
        val device = mAddress?.let { find(it) }


        services?.forEach { service ->
            if (service.uuid == null) return@forEach

            Log.d(HomeActivity.TAG, ">>>>>>>>> Gatt Service found : " + service?.uuid)

            if (service.uuid === LiftBT.configurationServiceUUID) {
                device?.controlService = service
            } else if (service.uuid === LiftBT.numberServiceUUID) {
                device?.numberService = service
            } else if (service.uuid === LiftBT.deviceControlServiceUUID) {
                device?.deviceService = service
            }

            // Loops through available GATT service characteristics.
            if (service.uuid == LiftBT.configurationServiceUUID || service.uuid == LiftBT.numberServiceUUID) {
                var linked = 0
                service.characteristics?.forEach { characteristic ->
                    when (characteristic.uuid) {
                        LiftBT.authCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found authorisation control")
                            device?.authControl = characteristic
                            linked += 1
                        }
                        LiftBT.levelsCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found audio levels control")
                            device?.audioControl = characteristic
                            linked += 1
                        }
                        LiftBT.phoneCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found phone number control")
                            device?.phoneControl = characteristic
                            linked += 1
                        }
                        LiftBT.phoneConfigCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found phone config control")
                            device?.phoneConfigControl = characteristic
                            linked += 1
                        }

                        LiftBT.jobCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found job/client control")
                            device?.jobControl = characteristic
                            linked += 1
                        }
                        LiftBT.infoCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found info control")
                            device?.infoControl = characteristic
                            linked += 1
                        }
                        LiftBT.wifiCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found wifi control")
                            device?.wifiControl = characteristic
                            linked += 1
                        }
                        LiftBT.ssidsCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found SSIDs control")
                            device?.ssisListControl = characteristic
                            linked += 1
                        }
                        LiftBT.gsmCharUUID -> {
                            Log.d(TAG, "[BT::Characteristic] - Found GSM status control")
                            device?.gsmControl = characteristic
                            linked += 1
                        }
                    }

                    val props = characteristic.properties
                    if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        if (waitIdle(LiftBT.GATT_TIMEOUT)) setCharacteristicNotification(characteristic, isEnabled)

                        Log.d(
                            HomeActivity.TAG,
                            ">>>>>>>>> Enable notification for: " + characteristic.uuid
                        )
                    }
                }

                if (linked > 0) {
                    device?.controlOk = isEnabled
                }
            } else if (service.uuid == LiftBT.deviceControlServiceUUID) {
                service.characteristics?.forEach { characteristic ->
                    Log.d(
                        TAG,
                        "[BT::Characteristic] - Found device control " + characteristic.uuid.toString()
                    )
                    /*
                     * 2A24 => Model Number
                     * 2A29 => Manufacturer Name
                     * 2A26 => Firmware Revision
                     */
                    when (characteristic.uuid) {
                        LiftBT.modelNumberCharUUID -> device?.modelNumControl = characteristic
                        LiftBT.manufacturerNameCharUUID -> device?.manNameControl =
                            characteristic
                        LiftBT.firmwareRevisionCharUUID -> device?.fwRevControl = characteristic
                    }
                }

                device?.deviceOK = isEnabled
            }

        }

        if (isEnabled) {
            // Start the modelNumber -> manufacturer -> firmwareRevision read chain once,
            // after the service loop - kicking it off per service started several
            // overlapping chains whose reads collided and silently died.
            val ser = mBluetoothGatt?.getService(LiftBT.deviceControlServiceUUID)
            val char = ser?.getCharacteristic(LiftBT.modelNumberCharUUID)
            if (char != null) readCharacteristicWithRetry(char)

            if (waitIdle(LiftBT.GATT_TIMEOUT)) broadcastUpdate(ACTION_SERVICES_UPDATED);
        } else {
            disconnect()
        }
        refreshScannedList()
    }

    fun isGsmSupported(): Boolean {
        val scanned = mAddress?.let { find(it) }
        return scanned?.gsmControl != null ||
                mBluetoothGatt?.services?.any { it.getCharacteristic(LiftBT.gsmCharUUID) != null } == true
    }

    private var gsmRequestThread: Thread? = null

    /**
     * Subscribes to and reads the GSM status characteristic.
     * The board requires authentication first - an unauthenticated read returns a single 0x11 byte
     * which processGsmDetail discards, and notifications are suppressed until authenticated, so the
     * CCCD write is forced even if a pre-auth subscription already cached the enabled value.
     * Runs on a worker thread because waitIdle()/readCharacteristic() block on the GATT busy flag.
     */
    fun requestGsmStatus(startDelayMillis: Long = 0) {
        if (gsmRequestThread?.isAlive == true) return

        gsmRequestThread = Thread {
            // Post-auth callers pass a delay so the device-info read chain
            // (model -> manufacturer -> firmware) gets the GATT slot first; the GSM state
            // arrives via notify right after subscription regardless.
            if (startDelayMillis > 0) {
                try { Thread.sleep(startDelayMillis) } catch (_: InterruptedException) {}
            }

            val scanned = mAddress?.let { find(it) }
            val characteristic = scanned?.gsmControl
                ?: mBluetoothGatt?.services?.firstNotNullOfOrNull { it.getCharacteristic(LiftBT.gsmCharUUID) }
                    ?.also { scanned?.gsmControl = it }

            if (characteristic == null) {
                Log.d(TAG, "[GSM] characteristic not available on this board")
                return@Thread
            }

            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                if (waitIdle(LiftBT.GATT_TIMEOUT)) setCharacteristicNotification(characteristic, true, force = true)
            }

            if (!readCharacteristicWithRetry(characteristic)) {
                Log.w(TAG, "[GSM] giving up reading GSM status, waiting for notify")
            }
        }.apply { start() }
    }

    fun broadcastUpdate(action: String, data: String? = null, clearBusy: Boolean = true) {
        if (clearBusy) mBusy = false
        val intent = Intent(action)
        if (data != null) intent.putExtra(ACTION_GATT_READ_DATA, data)
        LocalBroadcastManager.getInstance(this.applicationContext).sendBroadcast(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    fun close() {
        mBluetoothGatt?.let { gatt ->
            if (::timer.isInitialized) timer.cancel()
            if (checkPermission()) {
                gatt.disconnect()
                gatt.close()
            }
            mBluetoothGatt = null
        }
    }

    private fun setMtu() {
        if (checkPermission()) {
            mBusy = true
            val mtuRequest = mBluetoothGatt?.requestMtu(500)
            Log.i(TAG, "MTU Request: $mtuRequest: 500")
            if (mtuRequest != true) {
                // A rejected request produces no onMtuChanged callback - clear the busy
                // flag so the rest of updateServices is not stalled against it.
                mBusy = false
            }
        }
    }

    private fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic, enable: Boolean, force: Boolean = false
    ): Boolean {
        if (!checkGatt()) return false
        var ok = false
        // isNotificationEnabled reads the locally cached descriptor value, which a pre-auth
        // subscription attempt already set - force bypasses it to re-write the CCCD on the device.
        if (force || !isNotificationEnabled(characteristic)) {
            if (checkPermission() && waitIdle(LiftBT.GATT_TIMEOUT_FOR_NOTIFICATIONS)) {
                mBluetoothGatt?.setCharacteristicNotification(characteristic, enable)
                val clientConfig = characteristic.getDescriptor(LiftBT.CLIENT_CHARACTERISTIC_CONFIG)
                if (waitIdle(LiftBT.GATT_TIMEOUT_FOR_NOTIFICATIONS) && clientConfig != null) {
                    ok = if (enable) {
                        Log.i(TAG, "Enable notification: " + characteristic.uuid.toString())
                        clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                    } else {
                        Log.i(
                            TAG, "Disable notification: " + characteristic.uuid.toString()
                        );
                        clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
                    }

                    if (ok) {
                        mBusy = true

                        ok = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mBluetoothGatt?.writeDescriptor(
                                clientConfig,
                                if(enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                            ) == BluetoothStatusCodes.SUCCESS
                        } else {
                            mBluetoothGatt?.writeDescriptor(clientConfig)!!
                        }

                        if (ok) {
                            Log.i(TAG, "writeDescriptor: " + characteristic.uuid.toString())
                        } else {
                            // Rejected descriptor write produces no callback - clear the
                            // busy flag so later operations are not stalled against it.
                            mBusy = false
                            Log.w(TAG, "writeDescriptor rejected for " + characteristic.uuid.toString())
                        }
                    }
                }
            }
        }

        return ok
    }

    private fun isNotificationEnabled(
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        if (!checkGatt()) return false
        val clientConfig = characteristic
            .getDescriptor(LiftBT.CLIENT_CHARACTERISTIC_CONFIG) ?: return false
        return clientConfig.value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    }

    private fun checkGatt(): Boolean {
        if (mBluetoothAdapter == null) {
            return false
        }
        if (mBluetoothGatt == null) {
            return false
        }

        return !mBusy
    }

    fun waitIdle(timeout: Int): Boolean {
        // The previous /50 + pre-decrement loop waited at most timeout-50ms (a 100ms
        // timeout waited only 50ms) and reported failure even when the bus became idle
        // during the final sleep. Wait the full budget and return the actual state.
        var remaining = timeout
        while (remaining > 0) {
            if (!mBusy) return true
            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            remaining -= 50
        }
        return !mBusy
    }

    fun updatePin(pin: PINNumber) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift == null || !lift.connected || lift.authControl == null) {
            return
        }

        if (waitIdle(LiftBT.GATT_TIMEOUT)) {
            val token1 = if (S515LiftConfigureApp.profileStore.hasEngineerCapability) ProfileStore.EngineerTokenKey else device?.lift?.accessKey?.code()
            if (token1 != null) {
                val b1 = ((token1 shr 24) and 0xff).toByte()
                val b2 = ((token1 shr 16) and 0xff).toByte()
                val b3 = ((token1 shr 8) and 0xff).toByte()
                val b4 = ((token1 and 0xff)).toByte()

                val token2 = pin.code()
                val x1 = ((token2 shr 24) and 0xff).toByte()
                val x2 = ((token2 shr 16) and 0xff).toByte()
                val x3 = ((token2 shr 8) and 0xff).toByte()
                val x4 = ((token2 and 0xff)).toByte()

                val command: ByteArray = byteArrayOf(
                    S515BTCommand.btCmdAuthModify.toByte(),
                    0x08,b1,b2,b3,b4,x1,x2,x3,x4
                )
                lift.authControl!!.value = command
                val success = writeCharacteristic(lift.authControl!!, value = command)
                Log.d(TAG, "Characteristic written for auth modify: $success")
            }
        }
    }

    fun disconnect() {
        val lift = device?.lift?.let { find(it.liftId) } ?: return

        try {
            if (waitIdle(LiftBT.GATT_TIMEOUT)) {
                val command : ByteArray = byteArrayOf(S515BTCommand.btCmdDisconnect.toByte(), 0x0)
                lift.authControl?.value = command
                val success = writeCharacteristic(lift.authControl!!, value = command)
                Log.d(TAG, "Characteristic written for auth disconnect: $success")
            }
        } catch (_: Exception) {
            Log.e(TAG, "Unable to send disconnect command.")
        }

        close()
        scanLeDevice()
    }

    fun setVolume(volume: Int) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.audioControl == null) return

        val m = (device?.microphoneLevel ?: 0) + 3
        Log.d(TAG,"[BT::WRITE] audio level (volume=($volume), sensitivity=($m))")
        val command : ByteArray = byteArrayOf(S515BTCommand.btCmdSetVolumeAndSensitivity.toByte(), 0x02, volume.toByte() , m.toByte())
        lift.audioControl?.value = command
        val success = writeCharacteristic(lift.audioControl!!, value = command)
        Log.d(TAG, "Characteristic written for audio control volume: $success")
    }

    fun setMicrophone(microphone: Int) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.audioControl == null) return

        val vol = device?.volumeLevel ?: 1
        Log.d(TAG,"[BT::WRITE] audio level (volume=(${vol}), sensitivity=(${ + 3}))")
        val command : ByteArray = byteArrayOf(S515BTCommand.btCmdSetVolumeAndSensitivity.toByte(), 0x02, vol.toByte() , (microphone + 3).toByte())
        lift.audioControl?.value = command
        val success = writeCharacteristic(lift.audioControl!!, value = command)
        Log.d(TAG, "Characteristic written for audio control microphone: $success")
    }

    fun setPhoneNumber(phoneNumber : Int, enabled : Boolean, toNumber : String, name : String) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneControl == null) return

        if (toNumber.isNotEmpty()) {
            print("[BT::WRITE] phone slot(($phoneNumber) with number ($toNumber)")
            val numberData = toNumber.toByteArray()
            val nameData = name.toByteArray()
            val command: ByteArray = byteArrayOf(
                S515BTCommand.btCmdWritePhoneNumber.toByte(),
                (numberData.size + nameData.size + 3).toByte(),
                numberData.size.toByte(),
                phoneNumber.toByte(),
                (if (enabled) 0x01 else 0x00).toByte()
            ) + numberData + nameData
            lift.phoneControl?.value = command
            val success = writeCharacteristic(lift.phoneControl!!, value = command)
            Log.d(TAG, "Characteristic written for phone number: $success")
        } else {
            val command: ByteArray = byteArrayOf(
                S515BTCommand.btCmdClearPhoneNumber.toByte(),
                0x02.toByte(),
                0x00.toByte(),
                phoneNumber.toByte()
            )
            lift.phoneControl?.value = command
            val success = writeCharacteristic(lift.phoneControl!!, value = command)
            Log.d(TAG, "Characteristic written for phone number: $success")
        }
    }

    fun setPressDelay(pressDelay: Int) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        Log.d(TAG, "[BT::WRITE] call press delay ($pressDelay)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetCallPressDelay.toByte(), 0x01, pressDelay.toByte())
        lift.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for press delay: $success")
    }

    fun setDialTimeout(dialTimeout : Int) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        Log.d(TAG, "[BT::WRITE] call press delay ($dialTimeout)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetDialTimeoutDelay.toByte(), 0x01, dialTimeout.toByte())
        lift.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for dial timeout: $success")
    }

    fun setSimType(simType : SimType) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        Log.d(TAG, "[BT::WRITE] SIM Type ($simType)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetModemSimType.toByte(), 0x01, simType.ordinal.toByte())
        lift.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for sim type: $success")
    }

    fun setPin(pin : PINNumber) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        print("[BT::WRITE] SIM PIN (${pin.display()}) :: code=(${pin.code()})")
        val p1 = (if (pin.length >= 1) pin.digits[0] else 0).toByte()
        val p2 = (if (pin.length >= 2) pin.digits[1] else 0).toByte()
        val p3 = (if (pin.length >= 3) pin.digits[2] else 0).toByte()
        val p4 = (if (pin.length >= 4) pin.digits[3] else 0).toByte()
        val p5 = (if (pin.length >= 5) pin.digits[4] else 0).toByte()
        val p6 = (if (pin.length >= 6) pin.digits[5] else 0).toByte()
        val p7 = (if (pin.length >= 7) pin.digits[6] else 0).toByte()
        val p8 = (if (pin.length >= 8) pin.digits[7] else 0).toByte()

        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetSIMPin.toByte(), (10).toByte(), 0x01, pin.length.toByte(), p1, p2, p3, p4, p5, p6, p7, p8)
        val success = writeCharacteristic(lift.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for PinNumber: $success")
    }

    fun clearPin() {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetSIMPin.toByte(), (10).toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        lift.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for PinNumber: $success")
    }

    fun setSSID(ssid: String, passPhase: String, securityType: Int) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.wifiControl == null) return

        val ssidData = ssid.toByteArray()
        val passPhaseData = passPhase.toByteArray()
        val securityTypeData = securityType.toString().toByteArray()
        print("[BT::WRITE] Wifi Detail (ssid=($ssid), pkey=($passPhase), security=($securityType))")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetSSIDAndKey.toByte(), 0x00, ssidData.size.toByte(), passPhaseData.size.toByte(), securityTypeData.size.toByte()) + ssidData + passPhaseData + securityTypeData
        lift.wifiControl?.value = command
        val success = writeCharacteristic(lift.wifiControl!!, value = command)
        Log.d(TAG, "Characteristic written for SSID: $success")
    }

    fun setJob(job : String, client : String) {
        broadcastUpdate(ACTION_UPDATING_LIFT_SETTING, clearBusy = false)
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.jobControl == null) return

        val jobData = job.toByteArray(Charsets.UTF_8)
        val clientData = client.toByteArray(Charsets.UTF_8)
        Log.d(TAG, "[BT::WRITE] Job Detail (Job=($job), Client=($client)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetJobAndClient.toByte(), jobData.size.toByte(), clientData.size.toByte()) + jobData + clientData
//        lift.jobControl?.value = command
        val success = writeCharacteristic(lift.jobControl!!, value = command)
        Log.d(TAG, "Characteristic written for JobAndClient: $success, $command")
    }

    companion object {
        const val ACTION_BLUETOOTH_ON =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_BLUETOOTH_ON"
        const val ACTION_BLUETOOTH_OFF =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_BLUETOOTH_OFF"
        const val ACTION_BLUETOOTH_DEVICE_SCANNING =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_BLUETOOTH_DEVICE_SCANNING"
        const val ACTION_BLUETOOTH_DEVICE_SCANNING_STOPPED =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_BLUETOOTH_DEVICE_SCANNING_STOPPED"
        const val ACTION_BLUETOOTH_DEVICE_FOUND =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_BLUETOOTH_DEVICE_FOUND"
        const val ACTION_GATT_CONNECTING =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_GATT_CONNECTING"
        const val ACTION_GATT_CONNECTED =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_CONNECTION_FAILURE =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_GATT_CONNECTION_FAILURE"
        const val ACTION_GATT_DISCONNECTED =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_GATT_SERVICES_AUTHENTICATED =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_GATT_SERVICES_AUTHENTICATED"
        const val ACTION_GATT_READ_DATA =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_GATT_READ_DATA"
        const val ACTION_UPDATE_AUTHENTICATION =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_AUTHENTICATION"
        const val ACTION_CONNECTION_UPDATE =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_CONNECTION_UPDATE"
        const val ACTION_UPDATE_LEVEL =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_LEVEL"
        const val ACTION_UPDATE_INFO =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_INFO"
        const val ACTION_UPDATE_PHONE_CONFIG =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_PHONE_CONFIG"
        const val ACTION_UPDATE_PHONE_SLOT =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_PHONE_SLOT"
        const val ACTION_CLEAR_PHONE_SLOT =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_CLEAR_PHONE_SLOT"
        const val ACTION_UPDATE_JOB = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_JOB"
        const val ACTION_UPDATE_WIFI_DETAIL = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_WIFI_DETAIL"
        const val ACTION_UPDATE_SSID_LIST = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_SSID_LIST"
        const val ACTION_UPDATE_GSM_DETAIL = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_GSM_DETAIL"
        const val ACTION_LIFT_LIST_UPDATED = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_LIFT_LIST_UPDATED"
        const val ACTION_SERVICES_UPDATED = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_SERVICES_UPDATED"
        const val ACTION_UPDATING_LIFT_SETTING = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATING_LIFT_SETTING"
        const val ACTION_UPDATED_LIFT_SETTING = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATED_LIFT_SETTING"

        const val TAG = "BluetoothLeService: "
        var service: BluetoothLeService? = null
        const val DataOK : UInt = 0x55u
    }
}

fun BluetoothLeService.processAuth(data : ByteArray, device : ScannedDevice) {
    Log.d(BluetoothLeService.TAG, "Got data Auth: $data")

    if (data.isNotEmpty()) {
        device.authorised = data[0].toUInt() == BluetoothLeService.DataOK
        Log.d(BluetoothLeService.TAG, "Got data Auth: $data authorized: ${device.authorised}")

        updateConnectionState(false)

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_AUTHENTICATION, device.authorised.toString())
        updateCount += 1

        // GSM status notifications are suppressed until authenticated, so (re)subscribe
        // and read the current state once authentication succeeds.
        if (device.authorised) {
            requestGsmStatus(startDelayMillis = 1000)
        }
    }
}

fun BluetoothLeService.processLevel(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data level: $data")
    if (data.isNotEmpty() && data[0].toUInt() == BluetoothLeService.DataOK && data.size == 3) {
        device?.volumeLevel = data[1].toInt()
        device?.microphoneLevel = data[2].toInt()

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_LEVEL )
        updateCount += 1
    }
}

fun BluetoothLeService.processInfo(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Info: $data")
    if (data.size >= 4) {
        Log.d(BluetoothLeService.TAG, "[INFO] Board information READ: (data.hexEncodedString())")
        val bt = data[1]
        val dip = data[2]
        val cap = data[3]
        val board = BoardInfo(BoardInfo.commsBoardType(bt.toInt()), dip.toUInt(), BoardCapabilitySet(cap.toUInt()))
        val json = S515LiftConfigureApp.json

        device?.commsBoard = board

        Log.d(BluetoothLeService.TAG, "Got data Info: $board")

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_INFO, json.encodeToString(BoardInfo.serializer(), board) )
        updateCount += 1
    } else {
         Log.d(BluetoothLeService.TAG, "[INFO] - no data received")
    }
}

fun BluetoothLeService.processPhoneConfig(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Phone Config: $data")
    if (data.size >= 14) {
        val callDialTimeout = data[1].toInt()
        val callStartDelay = data[2].toInt()
        val simType = data[3].toInt()
        val simPinActive = data[4].toInt()
        val simPinLength = data[5].toInt()
        val p1 = data[6].toInt()
        val p2 = data[7].toInt()
        val p3 = data[8].toInt()
        val p4 = data[9].toInt()
        val p5 = data[10].toInt()
        val p6 = data[11].toInt()
        val p7 = data[12].toInt()
        val p8 = data[13].toInt()

        var pin: PhoneSimPin? = null

        if (simPinActive == 0x01) pin = PhoneSimPin(true, PINNumber(simPinLength, intArrayOf(p1, p2, p3, p4, p5, p6, p7, p8)))

        Log.d(BluetoothLeService.TAG, "[PHONE] phone detail READ: (Start Delay=$callStartDelay, Timeout=$callDialTimeout, simType: $simType, pin: $pin")

        val obj = JSONObject()
        obj.put("pressDelay", callStartDelay)
        obj.put("dialTimeout", callDialTimeout)
        obj.put("simType", simType)
        obj.put("simPin", pin)

        device?.simPin = pin
        device?.simType = Util.getSimType(simType)
        device?.callDialTimeout = callDialTimeout
        device?.callPressDelay = callStartDelay

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG, obj.toString() )
        updateCount += 1
    } else {
         Log.d(BluetoothLeService.TAG, "[PHONE-CONFIG] - no data received")
    }
}

fun BluetoothLeService.processPhone(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Phone: $data")
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[PHONE READ: $data")

        device?.number1 = PhoneContact(numberType = PhoneNumberType.user_defined)
        device?.number2 = PhoneContact(numberType = PhoneNumberType.user_defined)
        device?.number3 = PhoneContact(numberType = PhoneNumberType.user_defined)
        device?.number4 = PhoneContact(numberType = PhoneNumberType.installer)
        device?.number5 = PhoneContact(numberType = PhoneNumberType.emergency_services)

        for (idx in 0 until 5) {
            val mult = (idx * 74)
            val flag = if ((1 + mult) < data.size) ((data[1 + mult].toInt() shl 8) + data[mult].toInt()) else 0

            if ((flag and 0x01) == 0x00) {
                broadcastUpdate(BluetoothLeService.ACTION_CLEAR_PHONE_SLOT, (idx + 1).toString() )
            } else if (data.size < 42 + mult) {
                Log.w(BluetoothLeService.TAG, "[PHONE] - truncated payload (${data.size} bytes) for slot ${idx + 1}, skipped")
            } else {
                // Kotlin infix shl binds looser than +, so the unparenthesized form
                // computed x shl (8 + y); parenthesize and mask both (signed) bytes.
                val numCallCount = ((data[3 + mult].toInt() and 0xFF) shl 8) + (data[2 + mult].toInt() and 0xFF)

                val n1dtmmin    = data[4 + mult].toInt()
                val n1dtmhour   = data[5 + mult].toInt()
                val n1dtmmday   = data[6 + mult].toInt()
                val n1dtmmon    = data[7 + mult].toInt()
                val n1dtmyear   = data[8 + mult].toInt()

                val n1vtmmin    = data[9 + mult].toInt()
                val n1vtmhour   = data[10 + mult].toInt()
                val n1vtmmday   = data[11 + mult].toInt()
                val n1vtmmon    = data[12 + mult].toInt()
                val n1vtmyear   = if (data.size > (13 + mult)) data[13 + mult].toInt() else 0

                /*
                 * Note the magic number 123 here.
                 * The board will provide a date offset from the year 1900 (so 2023 => value of 2023 - 1900 => 123)
                 * We can discount any year prior to the app release date (can't be retrospective) so a quick way of filtering out dates
                 */
                val lastDialled = if (n1dtmyear >= 123) PhoneDate(n1dtmyear, n1dtmmon, n1dtmmday, n1dtmhour, n1dtmmin, null) else null
                val lastVoice = if (n1vtmyear >= 123) PhoneDate(n1vtmyear, n1vtmmon, n1vtmmday, n1vtmhour, n1vtmmin, null) else null
                val phoneNum = data.copyOfRange(14 + mult, 42 + mult).dropLastWhile { it == 0.toByte() }.toByteArray()
                val name = if (data.size >= 74 + mult) data.copyOfRange(42 + mult, 74 + mult).dropLastWhile { it == 0.toByte() }.toByteArray() else ByteArray(0)
                val enabled = (flag and 0x04) == 0x04

                val slot = idx + 1
                if (phoneNum.isEmpty()) {
                    when(slot) {
                        1 -> device?.number1 = PhoneContact(numberType = PhoneNumberType.user_defined)
                        2 -> device?.number2 = PhoneContact(numberType = PhoneNumberType.user_defined)
                        3 -> device?.number3 = PhoneContact(numberType = PhoneNumberType.user_defined)
                        4 -> device?.number4 = PhoneContact(numberType = PhoneNumberType.installer)
                        5 -> device?.number5 = PhoneContact(numberType = PhoneNumberType.emergency_services)
                    }
                } else {
                    when(slot) {
                        1 -> device?.number1 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, String(name, Charsets.UTF_8), lastDialled, lastVoice, PhoneNumberType.user_defined)
                        2 -> device?.number2 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, String(name, Charsets.UTF_8), lastDialled, lastVoice, PhoneNumberType.user_defined)
                        3 -> device?.number3 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, String(name, Charsets.UTF_8), lastDialled, lastVoice, PhoneNumberType.user_defined)
                        4 -> device?.number4 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, String(name, Charsets.UTF_8), lastDialled, lastVoice, PhoneNumberType.installer)
                        5 -> device?.number5 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, "Emergency Services", lastDialled, lastVoice, PhoneNumberType.emergency_services)
                    }
                }

                Log.d(BluetoothLeService.TAG, "[Phone phoneSlot: ${idx + 1}, phoneNo: ${phoneNum.decodeToString()}, enabled: $enabled, numCallCount: $numCallCount, lastDialled: $lastDialled, lastVoice: $lastVoice")
                broadcastUpdate(BluetoothLeService.ACTION_UPDATE_PHONE_SLOT )
            }
        }
        updateCount += 1
    } else {
        Log.d(BluetoothLeService.TAG, "[PHONE] - no data received")
    }
}

//fun BluetoothLeService.processDelay(data: ByteArray, device: ScannedDevice) {
//    Log.d(BluetoothLeService.TAG, "Got data Delay: $data")
//    if (data.isNotEmpty()) {
//        Log.d(BluetoothLeService.TAG, "[Call Delays READ: $data")
//    } else {
//        Log.d(BluetoothLeService.TAG, "[Call Delays] - no data received")
//    }
//}

fun BluetoothLeService.processJob(data: ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Job: ${String(data, Charsets.UTF_8)}")
    if (data.size >= 61) {
        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] Job/Client READ: data")

        val job = data.copyOfRange(1, 60 + 1).dropLastWhile { it == 0.toByte() }.toByteArray()
        val client = data.copyOfRange(61, data.size).dropLastWhile { it == 0.toByte() }.toByteArray()

        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] Job=${String(job, Charsets.UTF_8)}, client=${String(client, Charsets.UTF_8)}")

        device?.job = String(job, Charsets.UTF_8)
        device?.client = String(client, Charsets.UTF_8)

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_JOB)
        updateCount += 1
    } else {
        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] - no data received")
    }
}

fun BluetoothLeService.processWifiDetail(data: ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Wifi Detail: $data")
    if (data.size >= 4) {
//        val security = data[1]
        val wifiStatus = data[2]
        val ssidLen = data[3].toInt() and 0xFF
        val ssid : String? = if (ssidLen > 0 && data.size >= 4 + ssidLen) String(data.copyOfRange(4, 4 + ssidLen), Charsets.UTF_8) else null

        val wifiAvailable = (wifiStatus.toInt() and 0x01) == 0x01
        val wifiConnected = (wifiStatus.toInt() and 0x02) == 0x02
        val ssidPresent = (wifiStatus.toInt() and 0x04) == 0x04

        val obj = JSONObject()
        obj.put("wifiAvailable", wifiAvailable)
        obj.put("wifiConnected", wifiConnected)
        obj.put("ssid", ssid)
        Log.d(BluetoothLeService.TAG, "[Wifi] SSID=$ssid")

        device?.wifiAvailable = wifiAvailable
        device?.wifiConnected = wifiConnected
        device?.connectedSSID = ssid

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL)
        updateCount += 1
    } else {
         Log.d(BluetoothLeService.TAG, "[Wifi] - no data received")
    }
}

/*
 * GSM status payload - 98 bytes, fixed, packed:
 *  byte  0      auth marker, must be 0x55 (an unauthenticated read returns a single 0x11 byte)
 *  bytes 1-16   mobile network state    (null terminated, zero padded)
 *  bytes 17-32  mobile voice network type
 *  bytes 33-48  service state
 *  bytes 49-64  IMS registration status
 *  byte  65     signal strength in dBm  (signed; 0 = not yet reported)
 *  bytes 66-97  network operator name   (UTF-8, null terminated, zero padded)
 */
fun BluetoothLeService.processGsmDetail(data: ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data GSM Detail: ${data.size} bytes")

    if (data.size < 98 || data[0].toUInt() != BluetoothLeService.DataOK) {
        Log.d(BluetoothLeService.TAG, "[GSM] - invalid or unauthenticated payload, discarded")
        return
    }

    fun field(from: Int, to: Int): String? {
        val value = String(
            data.copyOfRange(from, to).takeWhile { it != 0.toByte() }.toByteArray(),
            Charsets.UTF_8
        ).trim()
        return value.ifEmpty { null }
    }

    val networkState = field(1, 17)

    device?.apply {
        gsmNetworkState = networkState
        gsmVoiceNetworkType = field(17, 33)
        gsmServiceState = field(33, 49)
        gsmImsStatus = field(49, 65)
        gsmSignalStrength = data[65].toInt()
        gsmOperator = field(66, 98)
        gsmConnected = networkState.equals("Connected", ignoreCase = true) ||
                networkState.equals("Roaming", ignoreCase = true)

        Log.d(
            BluetoothLeService.TAG,
            "[GSM] state=$gsmNetworkState, type=$gsmVoiceNetworkType, " +
            "service=$gsmServiceState, ims=$gsmImsStatus, " +
            "signal=$gsmSignalStrength dBm, operator=$gsmOperator"
        )
    }

    // GSM notifies arrive unsolicited on every state change: do not clear the busy flag owned by
    // an unrelated in-flight GATT operation, and do not bump updateCount - settings screens use it
    // to confirm their own pending writes.
    broadcastUpdate(BluetoothLeService.ACTION_UPDATE_GSM_DETAIL, clearBusy = false)
}

fun BluetoothLeService.processSSIDList(data: ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data SSID List: $data")
    if (data.size >= 2) {
         Log.d(BluetoothLeService.TAG, "[SSID List] READ: $data")

        val numSSIDs = data[1]

        if (data[0].toUInt() == BluetoothLeService.DataOK && numSSIDs > 0) {
             Log.d(BluetoothLeService.TAG, "SSID - number of ssid in list = $numSSIDs")
            for (s in 0 until numSSIDs) {
                val se = 2 + s * 34
                if (data.size < se + 34) break
                val sx = data.copyOfRange(se, se + 34)
                Log.d(BluetoothLeService.TAG, "SSID($s) = (${sx.decodeToString()})")
            }
        }

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_SSID_LIST, null )
        updateCount += 1
    } else {
         Log.d(BluetoothLeService.TAG, "[SSID List] - no data received")
    }
}


fun BluetoothLeService.setName(name: String) {
    val lift = device?.lift

    if (lift != null) {
        S515LiftConfigureApp.profileStore.update(name, lift)
    }
}

fun BluetoothLeService.setAccess(access: PINNumber) {
    val lift = device?.lift

    if (lift != null) {
        updatePin(access)
        S515LiftConfigureApp.profileStore.update(access, lift)
    }
}

fun BluetoothLeService.setContact(contact: Int, toName : String) {
    val lift = device?.lift

    if (lift != null) {
        S515LiftConfigureApp.profileStore.set(contact, toName, lift)
    }
}

object S515BTCommand {
    const val btCmdAuthenticate             = 0x01
    const val btCmdAuthModify               = 0x02
    const val btCmdSetVolume                = 0x03
    const val btCmdSetMicrophoneSensitivity = 0x04
    const val btCmdSetVolumeAndSensitivity  = 0x05
    const val btCmdWritePhoneNumber         = 0x06
    const val btCmdClearPhoneNumber         = 0x07
    const val btCmdSetCallPressDelay        = 0x08
    const val btCmdSetDialTimeoutDelay      = 0x09
    const val btCmdSetModemSimType          = 0x0a
    const val btCmdSetJobAndClient          = 0x0b
    const val btCmdSetSSIDAndKey            = 0x0c
    const val btCmdClearSSIDAndKey          = 0x0d
    const val btCmdDisconnect               = 0x0e
    const val btCmdSetSIMPin                = 0x0f
}


