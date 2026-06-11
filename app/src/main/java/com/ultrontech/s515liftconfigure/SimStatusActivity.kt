package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivitySimStatusBinding
import com.ultrontech.s515liftconfigure.util.EdgeToEdgeUtils

class SimStatusActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivitySimStatusBinding
    // Resolved lazily: the singleton is null when the activity is restored after process death.
    private val bluetoothLeService: BluetoothLeService? get() = BluetoothLeService.service

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothLeService.ACTION_UPDATE_GSM_DETAIL -> {
                    Log.d(TAG, "ACTION_UPDATE_GSM_DETAIL received from lift")
                    updateGsmDetail()
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    // Without this, the screen keeps showing the last GSM snapshot as
                    // live after the BLE link drops.
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BluetoothLeService.service == null) {
            // Restored after process death: no BLE session to show status for.
            finish()
            return
        }

        binding = ActivitySimStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EdgeToEdgeUtils.handleRootWindowInsets(binding.root)
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        setupClickListeners()
        updateGsmDetail()
    }

    private fun setupClickListeners() {
        binding.footer.btnHome.setOnClickListener {
            var intent = Intent(this, MyProductsActivity::class.java)
            if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
                intent = Intent(this, EngineerHomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.footer.btnBack.setOnClickListener {
            finish()
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
            val intent = Intent(this@SimStatusActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@SimStatusActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@SimStatusActivity, TroubleshootingActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@SimStatusActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }

    private fun updateGsmDetail() {
        val device = bluetoothLeService?.device
        val unknown = resources.getString(R.string.gsm_unknown)

        val networkState = device?.gsmNetworkState
        val isConnected = device?.gsmConnected == true

        binding.txtSimConnectedStatus.text = networkState ?: unknown
        if (isConnected) {
            binding.txtSimConnectedStatus.setTextColor(resources.getColor(R.color.lightGreen, theme))
        } else {
            binding.txtSimConnectedStatus.setTextColor(resources.getColor(R.color.red, theme))
        }

        val signal = device?.gsmSignalStrength
        binding.imgSignalStrength.setImageResource(
            when {
                !isConnected || signal == null || signal == 0 -> R.drawable.ic_signal_none
                signal >= -85 -> R.drawable.ic_signal_strong
                signal >= -100 -> R.drawable.ic_signal_medium
                else -> R.drawable.ic_signal_weak
            }
        )

        binding.txtNetwork.text = device?.gsmOperator ?: unknown
        binding.txtMobileNetworkState.text = networkState ?: unknown
        binding.txtServiceState.text = device?.gsmServiceState ?: unknown
        binding.txtImsRegistrationStatus.text = device?.gsmImsStatus ?: unknown
        binding.txtSignalStrength.text = if (signal == null || signal == 0) unknown
            else resources.getString(R.string.gsm_dbm_format, signal)
        binding.txtMobileNetworkType.text = device?.gsmVoiceNetworkType ?: unknown
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_UPDATE_GSM_DETAIL)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        }
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(bluetoothReceiver, filter)
        // Request the current GSM state from the lift; further changes arrive via notify.
        bluetoothLeService?.requestGsmStatus()
        updateGsmDetail()
    }

    override fun onPause() {
        super.onPause()
        try {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth receiver not registered: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SimStatusActivity"
    }
}
