package com.ultrontech.s515liftconfigure.models

import com.ultrontech.s515liftconfigure.S515LiftConfigureApp
import kotlinx.serialization.*;
import java.util.UUID

@Serializable
data class ProfileStore (
    var userDevices : Array<UserLift> = emptyArray(),
    var userName: String = "",
    var hasEngineerCapability: Boolean = false,
    var allowBiometrics: Boolean = false
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

    init {
        with(S515LiftConfigureApp) {
            with(sharedPreferences) {
                userName = getString(KEY_PROFILE_USER_NAME, KEY_EMPTY_STRING).toString()
                hasEngineerCapability = getBoolean(KEY_PROFILE_ENGINEER_LOGGED_IN, KEY_FALSE)
                allowBiometrics = getBoolean(KEY_PROFILE_USER_USE_BIO, KEY_FALSE)
                val storeDevices = getString(KEY_PROFILE_USER_DEVICES, null)

                if (storeDevices != null) {
                    userDevices = json.decodeFromString(storeDevices)
                }
            }
        }
    }

    fun find(existingWithId : String): UserLift? {
        return userDevices.find {
            it.liftId == existingWithId
        }
    }

    fun remove(lift : UserLift): Boolean {
        val idx = userDevices.indexOf (lift)

        val l = userDevices.toMutableList()
        l.removeAt(idx)
        if (idx > -1) userDevices = l.toTypedArray()

        return saveDevices()
    }

    fun add(lift: UserLift): Boolean {
        val l = userDevices.find {
            it.liftId === lift.liftId
        }

        if (l != null) {
            return false
        }

        userDevices = userDevices.plus(lift)
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
                    commit()
                }
            }
        }

        return true
    }

    fun login(engineerCode : String): Boolean {
        var result = false

        with(S515LiftConfigureApp) {
            with(sharedPreferences) {
                val editor = edit()

                with(editor) {
                    val cx = engineerCode.toInt()
                    if (cx == EngineerTokenKey) {
                        hasEngineerCapability = true
                        result = true
                        putBoolean(KEY_PROFILE_ENGINEER_LOGGED_IN, true)
                        commit()
                    }
                }
            }
        }

        return result
    }

    fun update(userName: String) {
        this.userName = userName
        with(S515LiftConfigureApp) {
            with(sharedPreferences) {
                val editor = edit()

                with(editor) {
                    putString(KEY_PROFILE_USER_NAME, userName)
                    commit()
                }
            }
        }
    }

    fun logout() {
        with(S515LiftConfigureApp) {
            with(sharedPreferences) {
                val editor = edit()

                with(editor) {
                    hasEngineerCapability = false
                    putBoolean(KEY_PROFILE_ENGINEER_LOGGED_IN, false)
                    commit()
                }
            }
        }
    }

    companion object{
        const val EngineerTokenKey : Int = 0x5B300B1
        const val AddLiftPin = "112233"
    }
}