package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityBoardDetailBinding
import com.ultrontech.s515liftconfigure.models.BoardCapabilitySet
import com.ultrontech.s515liftconfigure.util.EdgeToEdgeUtils

class BoardDetailActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivityBoardDetailBinding
    // Resolved lazily: the singleton is null when the activity is restored after process death.
    private val bluetoothLeService: BluetoothLeService? get() = BluetoothLeService.service

    // The board details (firmware revision in particular) often arrive after this screen is
    // already open - re-render when the service broadcasts an update instead of showing the
    // one-shot snapshot taken in onCreate.
    private val deviceUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothLeService.ACTION_LIFT_LIST_UPDATED -> updateFirmwareRevision()
                BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL -> updateWifiDetail()
                BluetoothLeService.ACTION_UPDATE_INFO -> updateInfo()
                BluetoothLeService.ACTION_UPDATE_JOB -> updateJob()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BluetoothLeService.service == null) {
            // Restored after process death: no BLE session to show details for.
            finish()
            return
        }

        binding = ActivityBoardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EdgeToEdgeUtils.handleRootWindowInsets(binding.root)
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        binding.llBtnEditJob.setOnClickListener {
            val intent = Intent(this, ChangeJobActivity::class.java)
            startActivity(intent)
        }

        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }

        updateWifiDetail()
        updateInfo()
        updateJob()
        updateFirmwareRevision()

        binding.toolbar.optionBtn.setOnClickListener {
            if (binding.optionMenu.llOptionMenu.visibility == View.GONE) {
                binding.optionMenu.llOptionMenu.visibility = View.VISIBLE
            } else {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
            }
        }

        binding.optionMenu.llMenuAccount.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            val intent = Intent(this@BoardDetailActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@BoardDetailActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@BoardDetailActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@BoardDetailActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_LIFT_LIST_UPDATED)
            addAction(BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL)
            addAction(BluetoothLeService.ACTION_UPDATE_INFO)
            addAction(BluetoothLeService.ACTION_UPDATE_JOB)
        }
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(deviceUpdateReceiver, filter)

        updateWifiDetail()
        updateInfo()
        updateJob()
        updateFirmwareRevision()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(deviceUpdateReceiver)
    }

    private fun updateFirmwareRevision() {
        with(bluetoothLeService ?: return) {
            binding.firmwareRevision.text = device?.lift?.liftId?.let { find(it)?.firmwareRevision }
        }
    }

    private fun updateWifiDetail() {
        with(bluetoothLeService ?: return) {
            if (device?.connectedSSID != null) {
                binding.ssidConfiguredLabel.text = this@BoardDetailActivity.resources.getString(R.string.ssid_configured)
                binding.ssidConfiguredLabel.setTextColor(resources.getColor(R.color.text_color_title, theme))
            } else {
                binding.ssidConfiguredLabel.text = this@BoardDetailActivity.resources.getString(R.string.no_ssid_configured)
                binding.ssidConfiguredLabel.setTextColor(resources.getColor(R.color.dark_red, theme))
            }

            if (device?.wifiAvailable == true) {
                binding.wifiAvailableStatus.text = this@BoardDetailActivity.resources.getString(R.string.wifi_is_available_on_device)
                binding.wifiAvailableStatus.setTextColor(resources.getColor(R.color.text_color_title, theme))
            } else {
                binding.wifiAvailableStatus.text = this@BoardDetailActivity.resources.getString(R.string.wifi_not_available_status)
                binding.wifiAvailableStatus.setTextColor(resources.getColor(R.color.red, theme))
            }

            if (device?.wifiConnected == true) {
                binding.wifiConnectedStatus.text = this@BoardDetailActivity.resources.getString(R.string.wifi_connected)
                binding.wifiConnectedStatus.setTextColor(resources.getColor(R.color.text_color_title, theme))
            } else {
                binding.wifiConnectedStatus.text = this@BoardDetailActivity.resources.getString(R.string.wifi_is_not_connected)
                binding.wifiConnectedStatus.setTextColor(resources.getColor(R.color.red, theme))
            }
        }
    }

    private fun updateInfo() {
        with(bluetoothLeService ?: return) {
            val capabilities = device?.commsBoard?.capabilities ?: return

            fun bullet(enabled: Boolean) = ResourcesCompat.getDrawable(
                resources,
                if (enabled) R.drawable.circle_bullet_green else R.drawable.circle_bullet_red,
                theme
            )

            binding.capGSM.background = bullet(capabilities.contains(BoardCapabilitySet.gsm))
            binding.capDiagnostics.background = bullet(capabilities.contains(BoardCapabilitySet.diagnostics))
            binding.capWifi.background = bullet(capabilities.contains(BoardCapabilitySet.wifi))
            binding.capWifiAP.background = bullet(capabilities.contains(BoardCapabilitySet.wifi_softap))
        }
    }

    private fun updateJob() {
        with(bluetoothLeService ?: return) {
            if (device?.job != null && device?.job?.length!! > 0) {
                binding.jobLabel.visibility = View.VISIBLE
                binding.job.text = device?.job
                binding.jobLabel.text = resources.getString(R.string.job_name)
            } else {
                binding.jobLabel.visibility = View.GONE
                binding.job.text = resources.getString(R.string.job_not_configured)
                binding.job.setTextColor(resources.getColor(R.color.dark_red, theme))
            }

            if (device?.client != null && device?.client?.length!! > 0) {
                binding.clientLabel.visibility = View.VISIBLE
                binding.client.text = device?.client
                binding.clientLabel.text = resources.getString(R.string.client)
            } else {
                binding.clientLabel.visibility = View.GONE
                binding.client.text = resources.getString(R.string.job_not_configured)
                binding.client.setTextColor(resources.getColor(R.color.dark_red, theme))
            }
        }
    }
}