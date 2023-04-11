package com.ultrontech.s515liftconfigure

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.json.Json

class S515LiftConfigureApp: Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this

        json = Json { ignoreUnknownKeys = true }

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    companion object {
        lateinit var instance: S515LiftConfigureApp
        lateinit var sharedPreferences: SharedPreferences
        lateinit var json: Json
        const val TAG = "EasyAirApp"
        const val KEY_PROFILE_USER_DEVICES = "profile.user.devices"
    }
}