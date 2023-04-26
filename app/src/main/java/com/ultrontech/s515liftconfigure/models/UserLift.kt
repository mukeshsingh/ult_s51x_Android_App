package com.ultrontech.s515liftconfigure.models

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
    modemSimTypeUnknown,
    modemSimTypeInstallerProvided,
    modemSimTypeUserContract,
    modemSimTypeUserPAYG,
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
    var callDialTimeout  : UInt?        = null,
    var callPressDelay   : UInt?        = null,
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
)

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
)

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

    fun code(): UInt {
        var c: UInt = 0u
        digits.forEach {
            c += (c shl 8) + (it.toUInt() and 255u)
        }
        return c
    }

    fun display(): String {
        var c: String = ""
        digits.forEach {
            c += it
        }

        return c
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
