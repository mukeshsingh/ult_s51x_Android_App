package com.ultrontech.s515liftconfigure.models

import android.util.Log
import java.util.Date
import java.util.UUID
import kotlinx.serialization.*;

@Serializable
enum class LiftConnectionState {
    not_connected,
    connected_noauth,
    connected_auth,
    connect_error
}

enum class CommsBoardType {
    S515,
    S510,
    UnknownCommsBoard,
}

@Serializable
data class BoardCapabilitySet(
    var rawValue: UInt
) {
    fun getAll(): Array<BoardCapabilitySet> {
        return arrayOf(gsm, diagnostics, wifi, wifi_softap)
    }
    companion object {
        var gsm: BoardCapabilitySet = BoardCapabilitySet(1u shl 0)
        var diagnostics: BoardCapabilitySet = BoardCapabilitySet(1u shl 1)
        var wifi: BoardCapabilitySet = BoardCapabilitySet(1u shl 2)
        var wifi_softap: BoardCapabilitySet = BoardCapabilitySet(1u shl 3)
    }
}

@Serializable
data class BoardInfo (
    var board_type  : CommsBoardType,
    var dip : UInt,
    var capabilities : BoardCapabilitySet
) {
    override fun toString(): String {
        return "board_type: ${getBoardType()}, dip: $dip, capebilities: ${capabilities.getAll().joinToString { it.toString() }}"
    }

    fun getBoardType(): String {
        return when(board_type) {
            CommsBoardType.S510 -> "S510"
            CommsBoardType.S515 -> "S515"
            CommsBoardType.UnknownCommsBoard -> "Unknown Comms Board"
        }
    }
    companion object{
        fun commsBoardType(bt: Int): CommsBoardType {
            return when(bt) {
                0x1 -> CommsBoardType.S515
                0x2 -> CommsBoardType.S510
                else -> CommsBoardType.UnknownCommsBoard
            }
        }
    }
}

enum class BoardType {
    d1504,
    lut125,
    han125,
    micro6
}

enum class SimType {
    ModemSimTypeUnknown,
    ModemSimTypeInstallerProvided,
    ModemSimTypeUserContract,
    ModemSimTypeUserPAYG,
}

object Util {
    fun getSimTypeName(simType: SimType): String {
        return when(simType) {
            SimType.ModemSimTypeInstallerProvided -> "Installer"
            SimType.ModemSimTypeUserContract -> "User Contact"
            SimType.ModemSimTypeUserPAYG -> "PAYG"
            SimType.ModemSimTypeUnknown -> "Unknown"
        }
    }

    fun getSimType(simType: Int): SimType {
        return when(simType) {
            0x00 -> SimType.ModemSimTypeUnknown
            0x01 -> SimType.ModemSimTypeInstallerProvided
            0x02 -> SimType.ModemSimTypeUserContract
            0x03 -> SimType.ModemSimTypeUserPAYG
            else -> SimType.ModemSimTypeUnknown
        }
    }
}

data class DeviceCapability (
    var hasGSMCapability : Boolean,
    var hasWifiCapability : Boolean,
    var hasDiagnosticCapability : Boolean,
    var boardType : BoardType
)


enum class PhoneNumberType {
    user_defined,
    installer,
    emergency_services
}

data class PhoneContact (
    var populated : Boolean = false,
    var enabled : Boolean = false,
    var number : String? = null,
    var callCount : Int? = 0,
    var contactName : String? = null,
    var lastDialled : PhoneDate? = null,
    var lastVoice : PhoneDate? = null,
    var numberType : PhoneNumberType
)

data class Device (
    var connectionState : LiftConnectionState = LiftConnectionState.not_connected,

    var volumeLevel      : Int?         = null,
    var microphoneLevel  : Int?         = null,
    var number1          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.user_defined),
    var number2          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.user_defined),
    var number3          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.user_defined),
    var number4          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.installer),
    var number5          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.emergency_services),
    var callDialTimeout  : Int?        = null,
    var callPressDelay   : Int?         = null,
    var simType          : SimType?     = null,
    var job              : String?      = null,
    var client           : String?      = null,
    var ssid             : String?      = null,
    var pkey             : String?      = null,
    var commsBoard       : BoardInfo?   = null,
    var wifiAvailable    : Boolean      = false,
    var wifiConnected    : Boolean      = false,
    var connectedSSID    : String?      = null,
    var simPin           : PhoneSimPin? = null,

    var lift    : UserLift? = null
) {
    fun noPreviousCalls(): Boolean {
        return true
    }

    fun callReminderNeeded(): Boolean {
        return true
    }

}

data class PhoneSimPin (
    var active : Boolean,
    var pin : PINNumber?
)

data class LiftDevice (
    var connectionState : LiftConnectionState,
    var deviceName : String?,
    var pinCode : String?,
    var capabilities : DeviceCapability?,
)


data class PhoneDate (
    var year : Int,
    var month : Int,
    var day : Int,
    var hour : Int,
    var mins : Int,

    var targetDate : Date?
) {
    override fun toString(): String {
        return "$year-$month-$day $hour:$mins -> $targetDate"
    }
}

@Serializable
data class PINNumber (
    var length : Int,
    var digits : IntArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PINNumber

        if (length != other.length) return false
        if (!digits.contentEquals(other.digits)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = length
        result = 31 * result + digits.contentHashCode()
        return result
    }

    fun code(): Int {
        var c: Int = 0
        for(i in 0 until length) {
            c += (c shl 8) + (digits[i] and 255)
        }
        return c
    }

    fun display(): String {
        var c: String = ""
        for(i in 0 until length) {
            c += digits[i]
        }

        return c
    }

    override fun toString(): String {
        return "Length: $length -> Pin: ${digits.joinToString { it.toString() }}"
    }
}

@Serializable
data class UserLift(
    var id: String = UUID.randomUUID().toString(),
    var liftId: String,
    var liftName: String,
    var accessKey: PINNumber,
    var userContact1Name: String = "",
    var userContact2Name: String = "",
    var userContact3Name: String = "",
    var installerName: String = "",
    var emergencyName: String = "",
)

fun UserLift.update(name: String): UserLift {
    return UserLift(liftId = this.liftId, liftName = name, accessKey = this.accessKey)
}

fun UserLift.update(access: PINNumber): UserLift {
    return UserLift(liftId = this.liftId, liftName = this.liftName, accessKey = access)
}
