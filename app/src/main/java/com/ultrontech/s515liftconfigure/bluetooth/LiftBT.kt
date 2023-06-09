package com.ultrontech.s515liftconfigure.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import java.util.*

object LiftBT {
    val configurationServiceUUID    : UUID = UUID.fromString("FF50B30E-D7E2-4D93-8842-A7C4A57DFA06")
    val numberServiceUUID           : UUID = UUID.fromString("FF50B30E-D7E2-4D93-8842-A7C4A57DFA07")
    val diagnosticServiceUUID       : UUID = UUID.fromString("FF50B30E-D7E2-4D93-8842-A7C4A57DFA75")

    val deviceControlServiceUUID    : UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

    val modelNumberCharUUID         : UUID = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb")
    val firmwareRevisionCharUUID    : UUID = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb")
    val manufacturerNameCharUUID    : UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb")

    val authCharUUID                : UUID = UUID.fromString("FF51B30E-D7E2-4D93-8842-A7C4A57DFA08")
    val levelsCharUUID              : UUID = UUID.fromString("FF52B30E-D7E2-4D93-8842-A7C4A57DFB08")
    val phoneCharUUID               : UUID = UUID.fromString("AB53B30E-D7E2-4D93-8842-A7C4A57DFB01")
    val phoneConfigCharUUID         : UUID = UUID.fromString("AB53B30E-D7E2-4D93-8842-A7C4A57DFC02")
    val jobCharUUID                 : UUID = UUID.fromString("AB55B30E-D7E2-4D93-8842-A7C4A57DFB10")
    val infoCharUUID                : UUID = UUID.fromString("FA51B30E-D7E2-4D93-8842-A7C4A57DFA88")
    val wifiCharUUID                : UUID = UUID.fromString("AB54B30E-D7E2-4D93-8842-A7C4A57DFB09")
    val ssidsCharUUID               : UUID = UUID.fromString("AB54B30E-D7E2-4D93-8842-A7C4A57DFB11")

    val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    const val GATT_TIMEOUT = 500 // milliseconds
    const val GATT_TIMEOUT_FOR_NOTIFICATIONS = 100 // milliseconds

    fun isChunkedResponse(uuid: UUID?): Boolean {
        return when (uuid) {
            else -> false
        }
    }

    fun isExpectWriteResponse(uuid: UUID?): Boolean {
        return when (uuid) {
            phoneConfigCharUUID -> false
            else -> true
        }
    }
}

data class ScanDisplayItem (
    var id: String,
    var name : String,
    var connected : Boolean,
    var modelNumber : String?
)

data class ScannedDevice(
    var id : String, var name : String, var connected: Boolean, var ignore: Boolean, var peripheral : BluetoothDevice
) {
    var authorised : Boolean = false
    var deviceService : BluetoothGattService? = null
    var controlService : BluetoothGattService? = null
    var numberService : BluetoothGattService? = null
    var diagnosticService : BluetoothGattService? = null
    var audioControl : BluetoothGattCharacteristic? = null
    var authControl : BluetoothGattCharacteristic? = null
    var jobControl : BluetoothGattCharacteristic? = null
    var discControl : BluetoothGattCharacteristic? = null
    var modelNumControl : BluetoothGattCharacteristic? = null
    var manNameControl : BluetoothGattCharacteristic? = null
    var fwRevControl : BluetoothGattCharacteristic? = null
    var infoControl : BluetoothGattCharacteristic? = null
    var wifiControl : BluetoothGattCharacteristic? = null
    var phoneControl : BluetoothGattCharacteristic? = null
    var phoneConfigControl : BluetoothGattCharacteristic? = null
    var ssisListControl : BluetoothGattCharacteristic? = null
    var controlOk : Boolean = false
    var deviceOK : Boolean = false
    var modelNumber : String? = null
    var manufacturerName : String? = null
    var firmwareRevision : String? = null
}

enum class BluetoothState {
    Authenticated,
    Connecting,
    Connected,
    ConnectionFailure,
    NotConnected
}

