package com.ultrontech.s515liftconfigure.bluetooth

import android.Manifest
import android.app.Service
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
        return mBinder
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            Log.e(TAG, ">>>>>>>> BluetoothLeService LocalBinder getService.")
            return this@BluetoothLeService
        }
    }

    fun authenticate() {
        val btService = mBluetoothGatt?.getService(LiftBT.configurationServiceUUID)
        var success: Boolean? = false
        if (btService != null) {
            val characteristic =
                btService?.getCharacteristic(LiftBT.authCharUUID)

            if (characteristic != null) {
                characteristic?.value = mAccessAuthKey.toByteArray(Charsets.UTF_8)
                success = writeCharacteristic(characteristic)
                Log.d(TAG, "Characteristic written: $success")
            }
        }
    }

    fun authorise(lift : ScannedDevice, userLift : UserLift) {
        Log.d(TAG, "Connect to Device : ${lift.name}")

        if (!lift.connected || lift.authControl == null) {
            return
        }

        with(S515LiftConfigureApp) {
            if (waitIdle(LiftBT.GATT_TIMEOUT)) {
                var token = if (profileStore.hasEngineerCapability) ProfileStore.EngineerTokenKey else userLift.accessKey.code()
                var b1 = ((token shr 24) and 0xffu).toByte()
                var b2 = ((token shr 16) and 0xffu).toByte()
                var b3 = ((token shr 8) and 0xffu).toByte()
                var b4 = ((token and 0xffu)).toByte()

                var command :ByteArray = byteArrayOf(S515BTCommand.btCmdAuthenticate.toByte(), 0x04, b1, b2, b3, b4)
                var data =  command
                lift.authControl!!.value = data
                writeCharacteristic(lift.authControl)
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
                timer.cancel()
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
            if (characteristic?.uuid == LiftBT.authCharUUID) {
                mSessionId = characteristic?.value?.let { String(it, Charsets.UTF_8) }
                broadcastUpdate(ACTION_GATT_SERVICES_AUTHENTICATED, null)
            }
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
                Log.w(TAG, "Got data: $dataString")

                when (this?.uuid) {
                    LiftBT.authCharUUID -> devices[mAddress]?.let { processAuth(data, it) }
                    LiftBT.levelsCharUUID -> devices[mAddress]?.let { processLevel(data, it) }
                    LiftBT.infoCharUUID -> devices[mAddress]?.let { processInfo(data, it) }
                    LiftBT.phoneCharUUID -> devices[mAddress]?.let { processPhone(data, it) }
                    LiftBT.phoneConfigCharUUID -> devices[mAddress]?.let {
                        processPhoneConfig(
                            data,
                            it
                        )
                    }
                    LiftBT.jobCharUUID -> devices[mAddress]?.let { processJob(data, it) }
                    LiftBT.wifiCharUUID -> devices[mAddress]?.let { processWifiDetail(data, it) }
                    LiftBT.ssidsCharUUID -> devices[mAddress]?.let { processSSIDList(data, it) }

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

    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        if (!checkGatt()) return false
        if (waitIdle(LiftBT.GATT_TIMEOUT)) {
            mBusy = true
            return checkPermission() && mBluetoothGatt?.writeCharacteristic(characteristic) ?: false
        }
        return false
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
        val intent = Intent(action)
        if (data != null) intent.putExtra(ACTION_GATT_READ_DATA, data)
        sendBroadcast(intent)
        mBusy = false
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

    companion object {
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
    if (data.isNotEmpty()) {
        device.authorised = data[0] as UInt == BluetoothLeService.DataOK
        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_AUTHENTICATION, device.authorised.toString())
    }
}

fun BluetoothLeService.processLevel(data : ByteArray, device : ScannedDevice) {
    if (data.isNotEmpty() && data[0] as UInt == BluetoothLeService.DataOK && data.size == 3) {
        val obj = JSONObject()
        obj.put("volume", data[1] as Int)
        obj.put("Sensitivity", data[2] as Int)
        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_LEVEL, obj.toString() )
    }
}

fun BluetoothLeService.processInfo(data : ByteArray, device: ScannedDevice) {
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[INFO] Board information READ: (data.hexEncodedString())")
        val bt = data[1]
        val dip = data[2]
        val cap = data[3]
        val board = BoardInfo(BoardInfo.commsBoardType(bt.toInt()), dip.toUInt(), BoardCapabilitySet(cap.toUInt()))
        val json = S515LiftConfigureApp.json

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_INFO, json.encodeToString(BoardInfo.serializer(), board) )
    } else {
         Log.d(BluetoothLeService.TAG, "[INFO] - no data received")
    }
}

fun BluetoothLeService.processPhoneConfig(data : ByteArray, device: ScannedDevice) {
    if (data.isNotEmpty()) {
        val callDialTimeout = data[1].toUInt()
        val callStartDelay = data[2].toUInt()
        val simType = data[3].toUInt()
        val simPinActive = data[4].toInt()
        val simPinLength = data[5].toUInt()
        val p1 = data[6].toInt()
        val p2 = data[7].toInt()
        val p3 = data[8].toInt()
        val p4 = data[9].toInt()
        val p5 = data[10].toInt()
        val p6 = data[11].toInt()
        val p7 = data[12].toInt()
        val p8 = data[13].toInt()

        var pin: PINNumber? = null
        if (simPinActive == 0x01) pin = PINNumber(simPinLength.toInt(), intArrayOf(p1, p2, p3, p4, p5, p6, p7, p8))

        Log.d(BluetoothLeService.TAG, "[PHONE READ: $data")
        Log.d(BluetoothLeService.TAG, "[PHONE] phone detail READ: (Dial Timeout=$callDialTimeout")
        Log.d(BluetoothLeService.TAG, "[PHONE] phone detail READ: (Start Delay=$callStartDelay")

        val obj = JSONObject()
        obj.put("pressDelay", callStartDelay)
        obj.put("dialTimeout", callDialTimeout)
        obj.put("simType", simType)
        obj.put("simPin", pin)

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG, obj.toString() )
    } else {
         Log.d(BluetoothLeService.TAG, "[PHONE-CONFIG] - no data received")
    }
}


fun BluetoothLeService.processPhone(data : ByteArray, device: ScannedDevice) {
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[PHONE READ: $data")

        for (idx in 0 until 5) {
            val mult = (idx * 42)
            val flag = data[1 + mult].toInt() shl 8 + data[mult].toInt()

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
                val n1vtmyear   = data[13 + mult].toInt()

                /*
                 * Note the magic number 123 here.
                 * The board will provide a date offset from the year 1900 (so 2023 => value of 2023 - 1900 => 123)
                 * We can discount any year prior to the app release date (can't be retrospective) so a quick way of filtering out dates
                 */
                val lastDialled = if (n1dtmyear >= 123) PhoneDate(n1dtmyear, n1dtmmon, n1dtmmday, n1dtmhour, n1dtmmin, null) else null
                val lastVoice = if (n1vtmyear >= 123) PhoneDate(n1vtmyear, n1vtmmon, n1vtmmday, n1vtmhour, n1vtmmin, null) else null
                val phoneNum = data.copyOfRange(14 + mult, 42 + mult)
                val enabled = (flag and 0x04) == 0x04

                val obj = JSONObject()
                obj.put("phoneSlot", idx + 1)
                obj.put("phoneNum", phoneNum)
                obj.put("enabled", enabled)
                obj.put("numCallCount", numCallCount)
                obj.put("lastDialled", lastDialled)
                obj.put("lastVoice", lastVoice)

                broadcastUpdate(BluetoothLeService.ACTION_UPDATE_PHONE_SLOT, obj.toString() )
            }
        }
    } else {
        Log.d(BluetoothLeService.TAG, "[PHONE] - no data received")
    }
}

fun BluetoothLeService.processDelay(data: ByteArray, device: ScannedDevice) {
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[Call Delays READ: $data")
    } else {
        Log.d(BluetoothLeService.TAG, "[Call Delays] - no data received")
    }
}

fun BluetoothLeService.processJob(data: ByteArray, device: ScannedDevice) {
    if (data.isNotEmpty()) {
        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] Job/Client READ: data")

        val job = data.copyOfRange(1, 60 + 1)
        val client = data.copyOfRange(61, data.size)
        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] Job=$job, client=$client")

        val obj = JSONObject()
        obj.put("job", job)
        obj.put("client", client)

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_JOB, obj.toString() )
    } else {
        Log.d(BluetoothLeService.TAG, "[JOB/CLIENT] - no data received")
    }
}

fun BluetoothLeService.processWifiDetail(data: ByteArray, device: ScannedDevice) {
    if (data.isNotEmpty()) {
        val security = data[1]
        val wstatus = data[2]
        val sslen = data[3]
        val ssid : String? = if (sslen > 0) data.copyOfRange(4, 4 + sslen).decodeToString() else null

        val wifiAvailable = (wstatus.toInt() and 0x01) == 0x01
        val wifiConnected = (wstatus.toInt() and 0x02) == 0x02
        val ssidPresent = (wstatus.toInt() and 0x04) == 0x04

        Log.d(BluetoothLeService.TAG, "[Wifi] SSID READ: $data - SSID=$ssid")

        val obj = JSONObject()
        obj.put("wifiAvailable", wifiAvailable)
        obj.put("wifiConnected", wifiConnected)
        obj.put("ssid", ssid)

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL, obj.toString() )
    } else {
         Log.d(BluetoothLeService.TAG, "[Wifi] - no data received")
    }
}

fun BluetoothLeService.processSSIDList(data: ByteArray, device: ScannedDevice) {
    if (data.isNotEmpty()) {
         Log.d(BluetoothLeService.TAG, "[SSID List] READ: $data")

        val numSSIDs = data[1]

        if (data[0].toUInt() == BluetoothLeService.DataOK && numSSIDs > 0) {
             Log.d(BluetoothLeService.TAG, "SSID - number of ssid in list = $numSSIDs")
            for (s in 0 until numSSIDs) {
                val se = 2 + s * 34
                val sx = data.copyOfRange(se, se + 34)
                 Log.d(BluetoothLeService.TAG, "SSID($s) = ($sx)")
            }
        }

        broadcastUpdate(BluetoothLeService.ACTION_UPDATE_SSID_LIST, null )
    } else {
         Log.d(BluetoothLeService.TAG, "[SSID List] - no data received")
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


