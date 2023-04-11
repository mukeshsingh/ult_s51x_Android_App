package com.ultrontech.s515liftconfigure.models

import com.ultrontech.s515liftconfigure.S515LiftConfigureApp
import kotlinx.serialization.*;
import java.util.UUID

@Serializable
data class ProfileStore (
    var userDevices : Array<UserLift>,
    var userName: String,
    var hasEngineerCapability: Boolean,
    var allowBiometrics: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProfileStore

        if (!userDevices.contentEquals(other.userDevices)) return false
        if (userName != other.userName) return false
        if (hasEngineerCapability != other.hasEngineerCapability) return false
        if (allowBiometrics != other.allowBiometrics) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userDevices.contentHashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + hasEngineerCapability.hashCode()
        result = 31 * result + allowBiometrics.hashCode()
        return result
    }

    fun find(existingWithId : String): UserLift? {
        return userDevices.find {
            it.liftId === existingWithId
        }
    }

    fun remove(lift : UserLift): Boolean {
        val idx = userDevices.indexOf (lift)

        if (idx > -1) userDevices.drop(idx)

        return false
    }

    fun add(lift: UserLift): Boolean {
        val l = userDevices.find {
            it.liftId === lift.liftId
        }

        if (l != null) {
            return false
        }

        userDevices.plus(lift)
        return saveDevices()
    }

    fun set(contact: Int, toName: String, lift: UserLift): UserLift {
        val pos = userDevices.indexOf(lift)
        if (pos > -1) {
            var item = userDevices[pos]

            when (contact) {
                1 -> item.userContact1Name = toName
                2 -> item.userContact2Name = toName
                3 -> item.userContact3Name = toName
                4 -> item.installerName = toName
                5 -> item.emergencyName = toName
            }

            userDevices[pos] = item
            saveDevices()

            return item
        }

        return lift
    }


    fun update(pin: PINNumber, lift: UserLift): UserLift {
        val pos = userDevices.indexOf(lift)
        if (pos > -1) {
            var item = userDevices[pos]
            item.accessKey = pin
            userDevices[pos] = item
            saveDevices()
            return item
        }

        return lift
    }

    fun update(name: String, lift: UserLift): UserLift {
        val pos = userDevices.indexOf(lift)
        if (pos > -1) {
            var item = userDevices[pos]
            item.liftName = name
            userDevices[pos] = item
            saveDevices()
            return item
        }

        return lift
    }

    private fun saveDevices(): Boolean {
        print("SAVE DEVICES: $(userDevices)")

        with(S515LiftConfigureApp) {
            with(sharedPreferences) {
                val editor = edit()
                with(editor) {
                    putString(KEY_PROFILE_USER_DEVICES, json.encodeToString(userDevices))
                }
            }
        }

        return true
    }

    fun login(engineerCode : String): Boolean {
        var result = false
        val cx = engineerCode.toUInt()
        if (cx == EngineerTokenKey) {
            hasEngineerCapability = true
            result = true
        }

        return result
    }

    fun logout() {
        hasEngineerCapability = false
    }

    companion object{
        val EngineerTokenKey : UInt = 0x5B300B1u
    }
}