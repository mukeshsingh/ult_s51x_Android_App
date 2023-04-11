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
    s515,
    s510,
    unknownCommsBoard,
}


data class BoardCapabilitySet(
    var rawValue: UInt
) {
    init {
        gsm           = BoardCapabilitySet(1u shl 0)
        diagnostics   = BoardCapabilitySet(1u shl 1)
        wifi          = BoardCapabilitySet(1u shl 2)
        wifi_softap   = BoardCapabilitySet(1u shl 3)
    }

    companion object {
        lateinit var gsm: BoardCapabilitySet
        lateinit var diagnostics: BoardCapabilitySet
        lateinit var wifi: BoardCapabilitySet
        lateinit var wifi_softap: BoardCapabilitySet
    }
}

data class BoardInfo (
    var board_type  : CommsBoardType,
    var dip : UInt,
    var capabilities : BoardCapabilitySet
)

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
    var number : String?,
    var callCount : Int?,
    var contactName : String?,
    var lastDialled : PhoneDate?,
    var lastVoice : PhoneDate?,
    var numberType : PhoneNumberType
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
){

//    var label : String {
//        // TODO - return in locale format
//
//    }
}
@Serializable
data class PINNumber (
    var length : Int,
    var digits : UIntArray,
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
            c += (c shl 8) + (it and 255u)
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

@kotlinx.serialization.Serializable
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
