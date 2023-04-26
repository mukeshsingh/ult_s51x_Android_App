package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.models.*

class EngineerDetailsActivity : AppCompatActivity() {
    private lateinit var liftName: TextView
    private lateinit var deviceStatus: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnConnect: Button
    private lateinit var btnRemove: Button
    private val bluetoothLeService: BluetoothLeService = BluetoothLeService.service!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_engineer_details)

        liftName = findViewById(R.id.txt_lift_name)
        deviceStatus = findViewById(R.id.txt_device_status)
        btnConnect = findViewById(R.id.btn_connect)
        btnEdit = findViewById(R.id.btn_edit)
        btnRemove = findViewById(R.id.btn_remove)

        btnRemove.setOnClickListener {
            bluetoothLeService.device.lift?.let { it1 ->
                S515LiftConfigureApp.profileStore.remove(it1)
                finish()
            }
        }
    }

    fun linkDevice () {
        val liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)
        if (liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(liftId)
            var device = Device(lift = lift)
            bluetoothLeService.link(device)

            liftName.text = lift?.liftName ?: ""
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(deviceUpdateReceiver, updateIntentFilter())

        linkDevice()
    }

    override fun onPause() {
        unregisterReceiver(deviceUpdateReceiver)
        super.onPause()
    }

    private fun updateConnectState() {
        with(bluetoothLeService) {
            when(device.connectionState) {
                LiftConnectionState.connected_noauth -> {
                    deviceStatus.text = resources.getString(R.string.device_connected_no_auth)
                    btnConnect.visibility = View.GONE
                    btnEdit.visibility = View.GONE
                    device.lift?.let { bluetoothLeService.authorise(it) }
                }
                LiftConnectionState.connected_auth -> {
                    deviceStatus.text = resources.getString(R.string.device_connected)
                    btnConnect.visibility = View.GONE
                    btnEdit.visibility = View.VISIBLE
                }
                LiftConnectionState.not_connected -> {
                    deviceStatus.text = resources.getString(R.string.device_not_connected)
                    btnConnect.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                }
                LiftConnectionState.connect_error -> {
                    deviceStatus.text = resources.getString(R.string.device_connect_error)
                    btnConnect.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                }
            }
        }
    }

    private val deviceUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_CONNECTION_UPDATE -> {
                    Log.d(HomeActivity.TAG, "Device connecting.")

                    updateConnectState()
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
                BluetoothLeService.ACTION_BLUETOOTH_ON -> {
                    Log.d(HomeActivity.TAG, "ACTION_BLUETOOTH_ON.")
                }
                BluetoothLeService.ACTION_BLUETOOTH_OFF -> {
                    finish()
                }
            }
        }
    }
    private fun updateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_BLUETOOTH_ON)
            addAction(BluetoothLeService.ACTION_BLUETOOTH_OFF)
            addAction(BluetoothLeService.ACTION_CONNECTION_UPDATE)
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
}