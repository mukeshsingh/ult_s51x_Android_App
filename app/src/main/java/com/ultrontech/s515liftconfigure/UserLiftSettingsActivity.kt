package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityUserLiftSettingsBinding
import com.ultrontech.s515liftconfigure.models.Device
import com.ultrontech.s515liftconfigure.models.LiftConnectionState

class UserLiftSettingsActivity : LangSupportBaseActivity() {
    private lateinit var binding: ActivityUserLiftSettingsBinding
    private var liftId: String? = null
    private val bluetoothLeService: BluetoothLeService = BluetoothLeService.service!!
    private lateinit var liftName: TextView
    private var hasEngineerCapability: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserLiftSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hasEngineerCapability = S515LiftConfigureApp.profileStore.hasEngineerCapability
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

        binding.microphoneSensitivity.setOnClickListener {
            val intent = Intent(this, MicrophoneSensitivityActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.userContactDetail.setOnClickListener {
            val intent = Intent(this, UserContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.simInformation.setOnClickListener {
            val intent = Intent(this, ChangeSimInformationActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.engineerBoardDetail.setOnClickListener {
            val intent = Intent(this, BoardDetailActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.engineerContactDetail.setOnClickListener {
            val intent = Intent(this, ChangeEngineerContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.emergencyServiceDetails.setOnClickListener {
            val intent = Intent(this, ChangeEmergencyContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.userContactDetail.setOnClickListener {
            val intent = Intent(this, ChangeUserContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.callPressDelay.setOnClickListener {
            val intent = Intent(this, ChangeCallPressDelayActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.dialTimeout.setOnClickListener {
            val intent = Intent(this, ChangeDialTimeoutActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.userContactDetail.setOnClickListener {
            val intent = Intent(this, UserContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.footer.btnBack.setOnClickListener {
            finish()
        }
        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.removeLift.setOnClickListener {
            bluetoothLeService.device?.lift?.let { it1 ->
                S515LiftConfigureApp.profileStore.remove(it1)
                finish()
            }
        }

        binding.disconnectLift.setOnClickListener {
            bluetoothLeService.device?.lift?.let {
                linkDevice()
            }
        }

        if (hasEngineerCapability) {
            binding.engineerBoardDetail.visibility = View.VISIBLE
            binding.engineerContactDetail.visibility = View.VISIBLE
            binding.emergencyServiceDetails.visibility = View.VISIBLE
            binding.dialTimeout.visibility = View.VISIBLE
            binding.callPressDelay.visibility = View.VISIBLE
            binding.microphoneSensitivity.visibility = View.VISIBLE

            binding.br1.visibility = View.VISIBLE
            binding.br2.visibility = View.VISIBLE
            binding.br3.visibility = View.VISIBLE
            binding.br4.visibility = View.VISIBLE
            binding.br5.visibility = View.VISIBLE
            binding.br6.visibility = View.VISIBLE

            binding.disconnectLift.visibility = View.GONE
            binding.removeLift.visibility = View.GONE
        }

        // ****************** Option Menu Start ******************
        binding.toolbar.optionBtn.setOnClickListener {
            if (binding.optionMenu.llOptionMenu.visibility == View.GONE) {
                binding.optionMenu.llOptionMenu.visibility = View.VISIBLE
            } else {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
            }
        }

        binding.optionMenu.llMenuAccount.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            val intent = Intent(this@UserLiftSettingsActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@UserLiftSettingsActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@UserLiftSettingsActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@UserLiftSettingsActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(deviceUpdateReceiver, updateIntentFilter())
        linkDevice()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(deviceUpdateReceiver)
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

    private fun updateConnectState() {
        with(bluetoothLeService) {
            when(device?.connectionState) {
                LiftConnectionState.connected_noauth -> {
//                    deviceStatus.text = resources.getString(R.string.device_connected_no_auth)
//                    btnConnect.visibility = View.VISIBLE
//                    btnEdit.visibility = View.GONE
//                    showHideCards(View.GONE)
                    device?.lift?.let { bluetoothLeService.authorise(it) }
                }
                LiftConnectionState.connected_auth -> {
//                    deviceStatus.text = resources.getString(R.string.device_connected)
//                    btnConnect.visibility = View.GONE
//                    btnEdit.visibility = View.VISIBLE
//                    showHideCards(View.VISIBLE)
                }
                LiftConnectionState.not_connected -> {
//                    deviceStatus.text = resources.getString(R.string.device_not_connected)
//                    btnConnect.visibility = View.VISIBLE
//                    btnEdit.visibility = View.GONE
//                    showHideCards(View.GONE)
                }
                LiftConnectionState.connect_error -> {
//                    deviceStatus.text = resources.getString(R.string.device_connect_error)
//                    btnConnect.visibility = View.VISIBLE
//                    btnEdit.visibility = View.GONE
//                    showHideCards(View.GONE)
                }
                else -> {}
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
    private fun updateIntentFilter(): IntentFilter {
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