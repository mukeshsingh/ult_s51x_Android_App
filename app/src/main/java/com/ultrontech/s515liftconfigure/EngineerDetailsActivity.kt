package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothState
import com.ultrontech.s515liftconfigure.bluetooth.LiftBT
import com.ultrontech.s515liftconfigure.models.*

class EngineerDetailsActivity : AppCompatActivity() {
    var connectionState : LiftConnectionState = LiftConnectionState.not_connected

    var volumeLevel      : Int?         = null
    var microphoneLevel  : Int?         = null
    var number1          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.user_defined)
    var number2          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.user_defined)
    var number3          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.user_defined)
    var number4          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.installer)
    var number5          : PhoneContact = PhoneContact(false, numberType = PhoneNumberType.emergency_services)
    var callDialTimeout  : UInt?        = null
    var callPressDelay   : UInt?        = null
    var simType          : SimType?     = null
    var job              : String?      = null
    var client           : String?      = null
    var ssid             : String?      = null
    var pkey             : String?      = null
    var commsBoard       : BoardInfo?   = null
    var wifiAvailable    : Boolean      = false
    var wifiConnected    : Boolean      = false
    var connectedSSID    : String?      = null
    var simPin           : PhoneSimPin? = null

    var lift    : UserLift? = null

    lateinit var liftName: TextView
    lateinit var deviceStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_engineer_details)

        liftName = findViewById(R.id.txt_lift_name)
        deviceStatus = findViewById(R.id.txt_device_status)

        val liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)
        if (liftId != null) {
            lift = S515LiftConfigureApp.profileStore.find(liftId)
            liftName.text = lift?.liftName ?: ""
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(deviceUpdateReceiver, updateIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(deviceUpdateReceiver)
    }

    private val deviceUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTING -> {
                    Log.d(HomeActivity.TAG, "Device connecting.")
                }
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    Log.d(HomeActivity.TAG, "Device connected.")
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    Log.d(HomeActivity.TAG, "Device disconnected.")
                }
                BluetoothLeService.ACTION_UPDATE_INFO -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_INFO.")
                }
                BluetoothLeService.ACTION_UPDATE_LEVEL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_LEVEL.")
                }
                BluetoothLeService.ACTION_UPDATE_AUTHENTICATION -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_AUTHENTICATION.")
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_SLOT.")
                }
                BluetoothLeService.ACTION_CLEAR_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_CLEAR_PHONE_SLOT.")
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_CONFIG.")
                }
                BluetoothLeService.ACTION_UPDATE_JOB -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_JOB.")
                }
                BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_WIFI_DETAIL.")
                }
                BluetoothLeService.ACTION_UPDATE_SSID_LIST -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_SSID_LIST.")
                }
            }
        }
    }
    private fun updateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTING)
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_UPDATE_SSID_LIST)
            addAction(BluetoothLeService.ACTION_UPDATE_JOB)
            addAction(BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG)
            addAction(BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL)
            addAction(BluetoothLeService.ACTION_UPDATE_AUTHENTICATION)
            addAction(BluetoothLeService.ACTION_UPDATE_PHONE_SLOT)
            addAction(BluetoothLeService.ACTION_UPDATE_INFO)
            addAction(BluetoothLeService.ACTION_UPDATE_LEVEL)
            addAction(BluetoothLeService.ACTION_CLEAR_PHONE_SLOT)
        }
    }
    companion object{
        const val ACTION_CONNECTED = "com.ultrontech.s515liftconfigure.ACTION_CONNECTED"
        const val ACTION_DISCONNECTED = "com.ultrontech.s515liftconfigure.ACTION_DISCONNECTED"
        const val ACTION_UPDATE_DIAL_TIME_OUT = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_DIAL_TIME_OUT"
        const val ACTION_UPDATE_PRESS_DELAY = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_PRESS_DELAY"
        const val ACTION_UPDATE_BOARD_INFO = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_BOARD_INFO"
        const val ACTION_UPDATE_JOB = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_JOB"
        const val ACTION_UPDATE_WIFI_AVAILABLE = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_WIFI_AVAILABLE"
        const val ACTION_UPDATE_SIM_TYPE = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_SIM_TYPE"
        const val ACTION_UPDATE_SIM_PIN = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_SIM_PIN"
        const val ACTION_UPDATE_PHONE_SLOT = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_PHONE_SLOT"
        const val ACTION_UPDATE_VOLUME = "com.ultrontech.s515liftconfigure.ACTION_UPDATE_VOLUME"
        const val ACTION_CLEAR_PHONE_SLOT = "com.ultrontech.s515liftconfigure.ACTION_CLEAR_PHONE_SLOT"
        const val ACTION_AUTHENTICATED = "com.ultrontech.s515liftconfigure.ACTION_AUTHENTICATED"
    }
}