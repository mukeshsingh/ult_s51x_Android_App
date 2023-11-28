package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityUserLiftSettingsBinding
import com.ultrontech.s515liftconfigure.models.Device

class UserLiftSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserLiftSettingsBinding
    private var liftId: String? = null
    private val bluetoothLeService: BluetoothLeService = BluetoothLeService.service!!
    private lateinit var liftName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserLiftSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)
        liftName = binding.title
        binding.liftName.setOnClickListener { view ->
            val intent = Intent(this, ChangeLiftNameActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.liftPinNumber.setOnClickListener {
            val intent = Intent(this, ChangePinNumberActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }
      binding.volume.setOnClickListener {
          val intent = Intent(this, ChangeVolumeActivity::class.java)
          intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
          startActivity(intent)
      }

        binding.simInformation.setOnClickListener {
            val intent = Intent(this, ChangeSimInformationActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.footer.btnBack.setOnClickListener {
            finish()
        }
        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.removeLift.setOnClickListener {
            bluetoothLeService.device?.lift?.let { it1 ->
                S515LiftConfigureApp.profileStore.remove(it1)
                finish()
            }
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

    private fun linkDevice () {
        if (liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(liftId!!)
            if (lift != null) {
                var device = Device(lift = lift)
                bluetoothLeService.link(device)

                liftName.text = lift?.liftName ?: ""
            }
        }
    }

    private val deviceUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_CONNECTION_UPDATE -> {
                    Log.d(HomeActivity.TAG, "Device connecting.")

//                    updateConnectState()
                }
                BluetoothLeService.ACTION_UPDATE_INFO -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_INFO.")

//                    updateInfo()
                }
                BluetoothLeService.ACTION_UPDATE_LEVEL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_LEVEL.")

//                    updateVolume()
                }
                BluetoothLeService.ACTION_UPDATE_AUTHENTICATION -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_AUTHENTICATION.")
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_SLOT.")
//                    updatePhoneSlots()
                }
                BluetoothLeService.ACTION_CLEAR_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_CLEAR_PHONE_SLOT.")
//                    updatePhoneSlots()
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_CONFIG.")
//                    updatePhoneConfig()
                }
                BluetoothLeService.ACTION_UPDATE_JOB -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_JOB.")
//                    updateJob()
                }
                BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_WIFI_DETAIL.")
//                    updateWifiDetail()
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
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    bluetoothLeService?.updateServices()
                }
                BluetoothLeService.ACTION_SERVICES_UPDATED -> {
                    linkDevice()
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    this@UserLiftSettingsActivity?.let { it1 ->
                        S515LiftConfigureApp.instance.basicAlert(
                            it1, "Lift disconnected."
                        ) { finish() }
                    }
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
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothLeService.ACTION_SERVICES_UPDATED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        }
    }
}