package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.ScanDisplayItem
import com.ultrontech.s515liftconfigure.databinding.ActivityUserLiftSettingsBinding
import com.ultrontech.s515liftconfigure.fragments.SuccessAddLiftFragment
import com.ultrontech.s515liftconfigure.models.LiftConnectionState
import kotlinx.coroutines.Job

class UserLiftSettingsActivity : LangSupportBaseActivity() {
    private lateinit var binding: ActivityUserLiftSettingsBinding
    private var liftId: String? = null
    private val bluetoothLeService: BluetoothLeService = BluetoothLeService.service!!
    private lateinit var liftName: TextView
    private lateinit var successFragment: SuccessAddLiftFragment
    private var hasEngineerCapability: Boolean = false
    private val hideHandler = Handler(Looper.myLooper()!!)

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

        successFragment = SuccessAddLiftFragment()

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

        binding.userContactDetail.setOnClickListener {
            val intent = Intent(this, UserContactActivity::class.java)
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

        binding.footer.btnBack.setOnClickListener {
            finish()
        }
        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.removeLift.setOnClickListener {
            showRemovePopup()
        }

        binding.disconnectLift.setOnClickListener {
            showDisConnectPopup()
        }

        binding.confirmRemoveLift.llRemovePopup.setOnClickListener {
            binding.confirmRemoveLift.llRemovePopup.visibility = View.GONE
        }
        binding.confirmRemoveLift.btnYesRemove.setOnClickListener {
            binding.confirmRemoveLift.llRemovePopup.visibility = View.GONE
            bluetoothLeService.device?.lift?.let { it1 ->
                S515LiftConfigureApp.profileStore.remove(it1)
                finish()
            }
        }

        binding.confirmDisconnectLift.llDisconnectPopup.setOnClickListener {
            binding.confirmDisconnectLift.llDisconnectPopup.visibility = View.GONE
        }
        binding.confirmDisconnectLift.btnYesDisconnect.setOnClickListener {
            binding.confirmDisconnectLift.llDisconnectPopup.visibility = View.GONE

            successFragment.show(supportFragmentManager, "SuccessAddLiftFragment")
            hideHandler.postDelayed(hideSuccess, AUTO_HIDE_DELAY_MILLIS.toLong())
        }

        if (hasEngineerCapability) {
            binding.engineerBoardDetail.visibility = View.VISIBLE
            binding.engineerContactDetail.visibility = View.VISIBLE
            binding.emergencyServiceDetails.visibility = View.VISIBLE
            binding.dialTimeout.visibility = View.VISIBLE
            binding.callPressDelay.visibility = View.VISIBLE

            binding.br1.visibility = View.VISIBLE
            binding.br2.visibility = View.VISIBLE
            binding.br3.visibility = View.VISIBLE
            binding.br4.visibility = View.VISIBLE
            binding.br5.visibility = View.VISIBLE
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

        if (liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(liftId!!)
            if (lift != null) {
                liftName.text = lift.liftName
                successFragment.updateMsg(lift.liftName, resources.getString(R.string.has_been_disconnected))
            }
        }

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(deviceUpdateReceiver, updateIntentFilter())
        startTimerToCheckUpdate(10000)
    }

    fun preventClicks(view: View?) {}

    private fun hideLoader() {
        runOnUiThread {
            binding.loader.loaderView.visibility = View.GONE
        }
    }

    private fun showLoader() {
        runOnUiThread {
            binding.loader.loaderView.visibility = View.VISIBLE
        }
    }

    private fun showRemovePopup() {
        runOnUiThread {
            binding.confirmRemoveLift.llRemovePopup.visibility = View.VISIBLE
        }
    }

    private fun showDisConnectPopup() {
        runOnUiThread {
            binding.confirmDisconnectLift.llDisconnectPopup.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(deviceUpdateReceiver)
        BluetoothLeService.service?.disconnect()
        super.onDestroy()
    }

    private fun updateConnectState() {
        with(bluetoothLeService) {
            when(device?.connectionState) {
                LiftConnectionState.connected_noauth -> {
                    device?.lift?.let { bluetoothLeService.authorise(it) }
                }
                LiftConnectionState.connected_auth -> {
                }
                LiftConnectionState.not_connected -> {
                }
                LiftConnectionState.connect_error -> {
                }
                else -> {}
            }
        }
    }

    private var updateCountNeeded = 0

    fun checkUpdatedCount() {
        if (BluetoothLeService.service?.updateCount!! >= updateCountNeeded) {
            hideLoader()
            BluetoothLeService.service?.updateCount = 0
            timer.cancel()
        }
    }

    private lateinit var timer: Job
    fun startTimerToCheckUpdate(time: Long) {
        timer = S515LiftConfigureApp.instance.startCoroutineTimer(delayMillis = time) {
            Log.d(BluetoothLeService.TAG, "User Lift settings timer called")
            BluetoothLeService.service?.updateCount = 0

            hideLoader()

            if (!isMsgDialogVisible) {
                isMsgDialogVisible = true
                this@UserLiftSettingsActivity.let { it1 ->
                    S515LiftConfigureApp.instance.basicAlert(
                        it1, "Unable to connect to Lift. Please try again."
                    ) { finish() }
                }
            }
        }
    }

    var isMsgDialogVisible = false
    private val deviceUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_CONNECTION_UPDATE -> {
                    Log.d(HomeActivity.TAG, "Device connecting.")
                }
                BluetoothLeService.ACTION_CLEAR_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_``CLEAR_PHONE_SLOT.")
                }
                BluetoothLeService.ACTION_UPDATE_AUTHENTICATION -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_AUTHENTICATION.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_UPDATE_INFO -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_INFO.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_UPDATE_LEVEL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_LEVEL.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_SLOT.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_CONFIG.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_UPDATE_JOB -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_JOB.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_WIFI_DETAIL.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_UPDATE_SSID_LIST -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_SSID_LIST.")
                    checkUpdatedCount()
                }
                BluetoothLeService.ACTION_BLUETOOTH_ON -> {
                    Log.d(HomeActivity.TAG, "ACTION_BLUETOOTH_ON.")
                }
                BluetoothLeService.ACTION_BLUETOOTH_OFF -> {
                    finish()
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    bluetoothLeService.updateServices(true)
                }
                BluetoothLeService.ACTION_SERVICES_UPDATED -> {
                    updateConnectState()
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    if (!isMsgDialogVisible) {
                        isMsgDialogVisible = true
                        try {timer.cancel()} catch (_: Exception){}

                        this@UserLiftSettingsActivity.let { it1 ->
                            S515LiftConfigureApp.instance.basicAlert(
                                it1, "Lift disconnected."
                            ) { finish() }
                        }
                    }
                }
                BluetoothLeService.ACTION_UPDATING_LIFT_SETTING -> {
                    showLoader()

                    BluetoothLeService.service?.updateCount = 0
                    updateCountNeeded = 1
                    startTimerToCheckUpdate(2000)
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
            addAction(BluetoothLeService.ACTION_UPDATING_LIFT_SETTING)
        }
    }

    private val hideSuccess = Runnable {
        supportFragmentManager.beginTransaction().remove(successFragment).commit()
        finish()
    }

    companion object{
        var lift: ScanDisplayItem? = null
        private const val AUTO_HIDE_DELAY_MILLIS = 2000
    }
}