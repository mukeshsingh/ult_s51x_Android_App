package com.ultrontech.s515liftconfigure.bluetooth

import android.Manifest
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
    private lateinit var mBluetoothLeScanner: BluetoothLeScanner
    private var mScanning = false
    private val mHandler = Handler(Looper.getMainLooper())
    lateinit var mAccessAuthKey: String
    var device: Device? = null
    private var mSessionId: String? = null
    private var mBusy = false
    var lifts = listOf<ScanDisplayItem>()

    var devices: HashMap<String, ScannedDevice> = HashMap<String, ScannedDevice> ()

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
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
                return false
            }
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
        registerReceiver(mReceiver, filter)

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

        if (isLink) {
            device?.connectionState = LiftConnectionState.connected_noauth
        } else {
            if (lift?.connected == true && lift?.authorised == true) {
                device?.connectionState = LiftConnectionState.connected_auth
            } else if (lift?.connected == true) {
                device?.connectionState = LiftConnectionState.connected_noauth
            } else if (lift?.connected == false) {
                device?.connectionState = LiftConnectionState.not_connected
            } else {
                device?.connectionState = LiftConnectionState.connect_error
            }
        }
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
                    var token =
                        if (profileStore.hasEngineerCapability) ProfileStore.EngineerTokenKey else userLift.accessKey.code()
                    var b1 = ((token shr 24) and 0xff).toByte()
                    var b2 = ((token shr 16) and 0xff).toByte()
                    var b3 = ((token shr 8) and 0xff).toByte()
                    var b4 = ((token and 0xff)).toByte()

                    var command: ByteArray =
                        byteArrayOf(S515BTCommand.btCmdAuthenticate.toByte(), 0x04, b1, b2, b3, b4)

                    lift?.authControl?.value = command

                    val success = writeCharacteristic(lift?.authControl!!, value = command)
                    Log.d(Companion.TAG, "Characteristic written for auth: $success")
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
            if (mAddress != null && address == mAddress && mBluetoothGatt != null && bluetoothDevice?.connected == false) {
                // Log.d(TAG, "Re-use GATT connection");
                broadcastUpdate(ACTION_GATT_CONNECTING)

                return mBluetoothGatt!!.connect()
            }

            this.mAccessAuthKey = authKey
            return try {
                if (bluetoothDevice == null) {
                    scanLeDevice()
                    return false
                }
                // We want to directly connect to the device, so we are setting the
                // autoConnect parameter to false.
                 Log.d(TAG, "Create a new GATT connection.")
                broadcastUpdate(ACTION_GATT_CONNECTING)
                mBluetoothGatt?.close()
                mBluetoothGatt = bluetoothDevice.peripheral!!.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
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
            if (waitIdle(LiftBT.GATT_TIMEOUT)) {
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
                if (timer != null) timer.cancel()
                // successfully connected to the GATT Server

                device?.connected = true
                broadcastUpdate(ACTION_GATT_CONNECTED, null)

                if (waitIdle(LiftBT.GATT_TIMEOUT) && checkPermission()) {
                    mBluetoothGatt?.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                device?.connected = false
                close()
                broadcastUpdate(ACTION_GATT_DISCONNECTED, null)
                scanLeDevice()
            }

//            update(device!!)

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
//            if (characteristic?.uuid == LiftBT.authCharUUID) {
//                mSessionId = characteristic?.value?.let { String(it, Charsets.UTF_8) }
//                broadcastUpdate(ACTION_GATT_SERVICES_AUTHENTICATED, null)
//            }
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
                    when (characteristic.uuid) {
//                        LiftBT.controlCharacteristic.uuid -> validateControlData(bytes)
                    }

                    bytes = null
                }
            }
        } else {
            with(characteristic) {
                val data = this?.value
                val dataString = String(data!!, Charsets.UTF_8)

                when (this?.uuid) {
                    LiftBT.authCharUUID -> devices[mAddress]?.let { processAuth(data, it) }
                    LiftBT.levelsCharUUID -> processLevel(data)
                    LiftBT.infoCharUUID -> processInfo(data)
                    LiftBT.phoneCharUUID -> processPhone(data)
                    LiftBT.phoneConfigCharUUID -> processPhoneConfig(data)
                    LiftBT.jobCharUUID -> processJob(data)
                    LiftBT.wifiCharUUID -> processWifiDetail(data)
                    LiftBT.ssidsCharUUID -> processSSIDList(data)

                    LiftBT.modelNumberCharUUID -> {
                        val cx = data.decodeToString()
                        print("[BT($mAddress)::Char($uuid))] : value updated -> [$cx)]")
                        devices[mAddress]?.modelNumber = cx
                        mBusy = false
                        refreshScannedList()
                        val service = mBluetoothGatt?.getService(LiftBT.deviceControlServiceUUID)
                        val char = service?.getCharacteristic(LiftBT.manufacturerNameCharUUID)
                        readCharacteristic(char)
                    }

                    LiftBT.manufacturerNameCharUUID -> {
                        val cx = data.decodeToString()
                        print("[BT($mAddress)::Char($uuid))] : value updated -> [$cx)]")
                        devices[mAddress]?.manufacturerName = cx
                        mBusy = false
                        refreshScannedList()
                        val service = mBluetoothGatt?.getService(LiftBT.deviceControlServiceUUID)
                        val char = service?.getCharacteristic(LiftBT.firmwareRevisionCharUUID)
                        readCharacteristic(char)
                    }

                    LiftBT.firmwareRevisionCharUUID -> {
                        val cx = data.decodeToString()
                        print("[BT($mAddress)::Char($uuid))] : value updated -> [$cx)]")
                        devices[mAddress]?.firmwareRevision = cx
                        mBusy = false
                        refreshScannedList()
                    }

                    else -> Log.d(BluetoothLeService.TAG, "unknown characteristic")
                }
            }
        }
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean? {
        Log.d(TAG, "readCharacteristic check Gatt: " + checkGatt())
//        if (!checkGatt()) return false
        if (waitIdle(LiftBT.GATT_TIMEOUT)) {
            mBusy = true
            return checkPermission() && mBluetoothGatt?.readCharacteristic(characteristic) == true
        }

        return false
    }

    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, value: ByteArray): Int? {
        if (!checkGatt()) return 0
        if (waitIdle(LiftBT.GATT_TIMEOUT)) {
            mBusy = true
            if (checkPermission()) {
                characteristic?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        return mBluetoothGatt?.writeCharacteristic(
                                it,
                                value,
                                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                            )
                    } else {
                        return if (mBluetoothGatt?.writeCharacteristic(it) == true) 1 else 0
                    }
                }
            }
        }
        return 0
    }

    fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH_SCAN
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun scanLeDevice() {
        broadcastUpdate(ACTION_GATT_CONNECTING)

        if (!mScanning) { // Stops scanning after a pre-defined scan period.
            Log.d(TAG, ">>>>>>>>>>>>>>> scanLeDevice")
            mHandler.postDelayed({
                mScanning = false
                if (checkPermission()) mBluetoothLeScanner.stopScan(leScanCallback)
                if (devices.size == 0) {
                    broadcastUpdate(ACTION_GATT_CONNECTION_FAILURE)
                    scanLeDevice()
                }
            }, 10000)
            Log.d(TAG, ">>>>>>>>>>>>>>> scanLeDevice: $mScanning")

            mScanning = true
            mBluetoothLeScanner.startScan(leScanCallback)
        }
    }

    private fun stopScan() {
        if (checkPermission()) {
            mHandler.removeCallbacksAndMessages(null)
            mBluetoothLeScanner.stopScan(leScanCallback)
            mScanning = false
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (checkPermission()) {
                val devise = find(result.device.address)

                if ((result.device.name ?: "").uppercase(Locale.ROOT).startsWith("SAVARIA")) {
                    if(devise != null) {
                        broadcastUpdate(ACTION_BLUETOOTH_DEVICE_FOUND)
                        connect(result.device.address, result.device.name)
                    } else {
                        Log.d(
                            TAG,
                            "========> Device found: " + result.device.address + " " + result.device.name
                        )

                        stopScan()

                        devices[result.device.address] = ScannedDevice(
                            result.device.address,
                            result.device.name,
                            connected = false,
                            ignore = false,
                            result.device
                        )

                        refreshScannedList()
                        broadcastUpdate(ACTION_BLUETOOTH_DEVICE_FOUND)
                        connect(result.device.address, result.device.name)
                    }
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

    fun updateServices() {
        val services = mBluetoothGatt?.services

        setMtu()

        services?.forEach { service ->
            if (service.uuid == null) return@forEach

            Log.d(HomeActivity.TAG, ">>>>>>>>> Gatt Service found : " + service?.uuid)

            val device = find(mAddress!!)
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
                    }

                    val props = characteristic.properties
                    if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        if (waitIdle(LiftBT.GATT_TIMEOUT) != null) {
                            setCharacteristicNotification(characteristic, true)
                        }
                        Log.d(HomeActivity.TAG, ">>>>>>>>> Enable notification for: " + characteristic.uuid)
                    }
                }

                if (linked > 0) {
                    device?.controlOk = true
                }
            } else if (service.uuid == LiftBT.deviceControlServiceUUID){
                service.characteristics?.forEach { characteristic ->
                    Log.d(TAG, "[BT::Characteristic] - Found device control " + characteristic.uuid.toString())
                    /*
                     * 2A24 => Model Number
                     * 2A29 => Manufacturer Name
                     * 2A26 => Firmware Revision
                     */
                    when (characteristic.uuid) {
                        LiftBT.modelNumberCharUUID -> device?.modelNumControl = characteristic
                        LiftBT.manufacturerNameCharUUID -> device?.manNameControl = characteristic
                        LiftBT.firmwareRevisionCharUUID -> device?.fwRevControl = characteristic
                    }
                }

                device?.deviceOK = true
            }

            val service = mBluetoothGatt?.getService(LiftBT.deviceControlServiceUUID)
            val char = service?.getCharacteristic(LiftBT.modelNumberCharUUID)
            readCharacteristic(char)
            refreshScannedList()
        }
    }

    fun broadcastUpdate(action: String, data: String? = null) {
        mBusy = false
        val intent = Intent(action)
        if (data != null) intent.putExtra(ACTION_GATT_READ_DATA, data)
        sendBroadcast(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    fun close() {
        mBluetoothGatt?.let { gatt ->
            timer.cancel()
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
            val mtuRequest = mBluetoothGatt?.requestMtu(100)
            Log.i(TAG, "MTU Request: $mtuRequest: 100")
        }
    }

    private fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic, enable: Boolean
    ): Boolean {
        if (!checkGatt()) return false
        var ok = false
        if (!isNotificationEnabled(characteristic)) {
            if (checkPermission() && waitIdle(LiftBT.GATT_TIMEOUT) && mBluetoothGatt?.setCharacteristicNotification(characteristic, enable) == true) {
                if (waitIdle(LiftBT.GATT_TIMEOUT)) {
                    val clientConfig =
                        characteristic.getDescriptor(LiftBT.CLIENT_CHARACTERISTIC_CONFIG)
                    if (clientConfig != null) {
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
                            ok = mBluetoothGatt?.writeDescriptor(clientConfig) == true
                            Log.i(
                                TAG,
                                "writeDescriptor: " + characteristic.uuid.toString()
                            )
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
        return clientConfig.value == BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
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
        var timeout = timeout
        timeout /= 50
        while (--timeout > 0) {
            if (mBusy) try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } else break
        }
        return timeout > 0
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
                lift?.authControl!!.value = command
                val success = writeCharacteristic(lift?.authControl!!, value = command)
                Log.d(TAG, "Characteristic written for auth modify: $success")
            }
        }
    }

    fun disconnect() {
        val lift = device?.lift?.let { find(it.liftId) }
        if (!lift?.connected!!) {
            return
        }

        if (waitIdle(LiftBT.GATT_TIMEOUT)) {
            val command : ByteArray = byteArrayOf(S515BTCommand.btCmdDisconnect.toByte(), 0x0)
            lift?.authControl?.value = command
            val success = writeCharacteristic(lift?.authControl!!, value = command)
            Log.d(TAG, "Characteristic written for auth disconnect: $success")
        }
    }

    fun setVolume(volume: Int) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.audioControl != null) {
            val m = (device?.microphoneLevel ?: 0) + 3
            Log.d(TAG,"[BT::WRITE] audio level (volume=($volume), sensitivity=($m))")
            val command : ByteArray = byteArrayOf(S515BTCommand.btCmdSetVolumeAndSensitivity.toByte(), 0x02, volume.toByte() , m.toByte())
            lift?.audioControl?.value = command
            val success = writeCharacteristic(lift?.audioControl!!, value = command)
            Log.d(TAG, "Characteristic written for audio control volume: $success")
        }
    }

    fun setMicrophone(microphone: Int) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.audioControl == null) return

        val vol = device?.volumeLevel ?: 1
        Log.d(TAG,"[BT::WRITE] audio level (volume=(${vol}), sensitivity=(${ + 3}))")
        val command : ByteArray = byteArrayOf(S515BTCommand.btCmdSetVolumeAndSensitivity.toByte(), 0x02, vol.toByte() , (microphone + 3).toByte())
        lift?.audioControl?.value = command
        val success = writeCharacteristic(lift?.audioControl!!, value = command)
        Log.d(TAG, "Characteristic written for audio control microphone: $success")
    }

    fun setPhoneNumber(phoneNumber : Int, enabled : Boolean, toNumber : String) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneControl == null) return

        print("[BT::WRITE] phone slot(($phoneNumber) with number ($toNumber)")
        val numberData = toNumber.toByteArray()
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdWritePhoneNumber.toByte(), (numberData.size + 2).toByte(), phoneNumber.toByte(), (if (enabled) 0x01 else 0x00).toByte()) + numberData
        lift?.phoneControl?.value = command
        val success = writeCharacteristic(lift?.phoneControl!!, value = command)
        Log.d(TAG, "Characteristic written for phone number: $success")
    }

    fun setPressDelay(pressDelay: Int) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        Log.d(TAG, "[BT::WRITE] call press delay ($pressDelay)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetCallPressDelay.toByte(), 0x01, pressDelay.toByte())
        lift?.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift?.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for press delay: $success")
    }

    fun setDialTimeout(dialTimeout : Int) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        Log.d(TAG, "[BT::WRITE] call press delay ($dialTimeout)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetDialTimeoutDelay.toByte(), 0x01, dialTimeout.toByte())
        lift?.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift?.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for dial timeout: $success")
    }

    fun setSimType(simType : SimType) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        Log.d(TAG, "[BT::WRITE] call press delay ($simType)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetModemSimType.toByte(), 0x01, simType.ordinal.toByte())
        lift?.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift?.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for sim type: $success")
    }

    fun setPin(pin : PINNumber) {
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
        val p8 = (if (pin.length >= 8) pin.digits[8] else 0).toByte()

        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetSIMPin.toByte(), (10).toByte(), 0x01, pin.length.toByte(), p1, p2, p3, p4, p5, p6, p7, p8)
        val success = writeCharacteristic(lift?.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for PinNumber: $success")
    }

    fun clearPin() {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.phoneConfigControl == null) return

        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetSIMPin.toByte(), (10).toByte(), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        lift?.phoneConfigControl?.value = command
        val success = writeCharacteristic(lift?.phoneConfigControl!!, value = command)
        Log.d(TAG, "Characteristic written for PinNumber: $success")
    }

    fun setSSID(ssid: String, passPhase: String) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.wifiControl == null) return

        val ssidData = ssid.toByteArray()
        val passPhaseData = passPhase.toByteArray()
        print("[BT::WRITE] Wifi Detail (ssid=($ssid), pkey=($passPhase)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetSSIDAndKey.toByte(), 0x00, ssidData.size.toByte(), passPhaseData.size.toByte()) + ssidData + passPhaseData
        lift?.wifiControl?.value = command
        val success = writeCharacteristic(lift?.wifiControl!!, value = command)
        Log.d(TAG, "Characteristic written for PinNumber: $success")
    }

    fun setJob(job : String, client : String) {
        val lift = device?.lift?.let { find(it.liftId) }
        if (lift?.jobControl == null) return

        val jobData = job.toByteArray()
        val clientData = client.toByteArray()
        print("[BT::WRITE] Wifi Detail (ssid=($job), pkey=($client)")
        val command: ByteArray = byteArrayOf(S515BTCommand.btCmdSetJobAndClient.toByte(), jobData.size.toByte(), clientData.size.toByte()) + jobData + clientData
        lift?.jobControl?.value = command
        val success = writeCharacteristic(lift?.jobControl!!, value = command)
        Log.d(TAG, "Characteristic written for PinNumber: $success")
    }

    companion object {
        const val ACTION_BLUETOOTH_ON =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_BLUETOOTH_ON"
        const val ACTION_BLUETOOTH_OFF =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_BLUETOOTH_OFF"
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
        const val ACTION_UPDATE_JOB =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_JOB"
        const val ACTION_UPDATE_WIFI_DETAIL =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_WIFI_DETAIL"
        const val ACTION_UPDATE_SSID_LIST =
            "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_UPDATE_SSID_LIST"
        const val ACTION_LIFT_LIST_UPDATED = "com.ultrontech.s515liftconfigure.bluetooth.le.ACTION_LIFT_LIST_UPDATED"

        const val TAG = "BluetoothLeService: "
        var service: BluetoothLeService? = null
        const val DataOK : UInt = 0x55u
    }
}

fun BluetoothLeService.processAuth(data : ByteArray, device : ScannedDevice) {
    Log.d(BluetoothLeService.TAG, "Got data Auth: $data")

    if (data.isNotEmpty()) {
        device.authorised = data[0].toUInt() == BluetoothLeService.DataOK

        updateConnectionState(false)

        broadcastUpdate(BluetoothLeService.ACTION_CONNECTION_UPDATE, device.authorised.toString())
        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_AUTHENTICATION, device.authorised.toString())
    }
}

fun BluetoothLeService.processLevel(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data level: $data")
    if (data.isNotEmpty() && data[0].toUInt() == BluetoothLeService.DataOK && data.size == 3) {
        device?.volumeLevel = data[1].toInt()
        device?.microphoneLevel = data[2].toInt()
        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_LEVEL )
    }
}

fun BluetoothLeService.processInfo(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Info: $data")
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[INFO] Board information READ: (data.hexEncodedString())")
        val bt = data[1]
        val dip = data[2]
        val cap = data[3]
        val board = BoardInfo(BoardInfo.commsBoardType(bt.toInt()), dip.toUInt(), BoardCapabilitySet(cap.toUInt()))
        val json = S515LiftConfigureApp.json

        device?.commsBoard = board

        Log.d(BluetoothLeService.TAG, "Got data Info: $board")

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_INFO, json.encodeToString(BoardInfo.serializer(), board) )
    } else {
         Log.d(BluetoothLeService.TAG, "[INFO] - no data received")
    }
}

fun BluetoothLeService.processPhoneConfig(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Phone Config: $data")
    if (data.isNotEmpty()) {
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
    } else {
         Log.d(BluetoothLeService.TAG, "[PHONE-CONFIG] - no data received")
    }
}


fun BluetoothLeService.processPhone(data : ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Phone: $data")
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[PHONE READ: $data")

        for (idx in 0 until 5) {
            val mult = (idx * 42)
            val flag = if ((1 + mult) < data.size) ((data[1 + mult].toInt() shl 8) + data[mult].toInt()) else 0

            if ((flag and 0x01) == 0x00) {
                broadcastUpdate(BluetoothLeService.ACTION_CLEAR_PHONE_SLOT, (idx + 1).toString() )
            } else {
                val numCallCount = data[3 + mult].toInt() shl 8 + data[2 + mult]

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
                val phoneNum = data.copyOfRange(14 + mult, 42 + mult)
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
                        1 -> device?.number1 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, "Test Contact 1", lastDialled, lastVoice, PhoneNumberType.user_defined)
                        2 -> device?.number2 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, "Test Contact 2", lastDialled, lastVoice, PhoneNumberType.user_defined)
                        3 -> device?.number3 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, "Test Contact 3", lastDialled, lastVoice, PhoneNumberType.user_defined)
                        4 -> device?.number4 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, "Installers", lastDialled, lastVoice, PhoneNumberType.installer)
                        5 -> device?.number5 = PhoneContact(true, enabled, String(phoneNum, Charsets.UTF_8), numCallCount, "Emergency Services", lastDialled, lastVoice, PhoneNumberType.emergency_services)
                    }
                }

                Log.d(BluetoothLeService.TAG, "[Phone phoneSlot: ${idx + 1}, phoneNo: ${phoneNum.decodeToString()}, enabled: $enabled, numCallCount: $numCallCount, lastDialled: $lastDialled, lastVoice: $lastVoice")
                broadcastUpdate(BluetoothLeService.ACTION_UPDATE_PHONE_SLOT )
            }
        }
    } else {
        Log.d(BluetoothLeService.TAG, "[PHONE] - no data received")
    }
}

fun BluetoothLeService.processDelay(data: ByteArray, device: ScannedDevice) {
    Log.d(BluetoothLeService.TAG, "Got data Delay: $data")
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[Call Delays READ: $data")
    } else {
        Log.d(BluetoothLeService.TAG, "[Call Delays] - no data received")
    }
}

fun BluetoothLeService.processJob(data: ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Job: $data")
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] Job/Client READ: data")

        val job = data.copyOfRange(1, 60 + 1).dropLastWhile { it == 0.toByte() }.toByteArray()
        val client = data.copyOfRange(61, data.size).dropLastWhile { it == 0.toByte() }.toByteArray()

        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] Job=${String(job, Charsets.UTF_8)}, client=${String(client, Charsets.UTF_8)}")

        device?.job = String(job, Charsets.UTF_8)
        device?.client = String(client, Charsets.UTF_8)

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_JOB)
    } else {
        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] - no data received")
    }
}

fun BluetoothLeService.processWifiDetail(data: ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data Wifi Detail: $data")
    if (data.isNotEmpty()) {
        val security = data[1]
        val wifiStatus = data[2]
        val ssidLen = data[3]
        val ssid : String? = if (ssidLen > 0) String(data.copyOfRange(4, 4 + ssidLen).dropLastWhile { it == 0.toByte() }.toByteArray(), Charsets.UTF_8) else null

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
    } else {
         Log.d(BluetoothLeService.TAG, "[Wifi] - no data received")
    }
}

fun BluetoothLeService.processSSIDList(data: ByteArray) {
    Log.d(BluetoothLeService.TAG, "Got data SSID List: $data")
    if (data.isNotEmpty()) {
         Log.d(BluetoothLeService.TAG, "[SSID List] READ: $data")

        val numSSIDs = data[1]

        if (data[0].toUInt() == BluetoothLeService.DataOK && numSSIDs > 0) {
             Log.d(BluetoothLeService.TAG, "SSID - number of ssid in list = $numSSIDs")
            for (s in 0 until numSSIDs) {
                val se = 2 + s * 34
                val sx = data.copyOfRange(se, se + 34)
                Log.d(BluetoothLeService.TAG, "SSID($s) = (${sx.decodeToString()})")
            }
        }

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_SSID_LIST, null )
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


