package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.SeekBar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeVolumeBinding
import com.ultrontech.s515liftconfigure.databinding.ActivityMicrophoneSensitivityBinding
import com.ultrontech.s515liftconfigure.util.EdgeToEdgeUtils

class MicrophoneSensitivityActivity : LangSupportBaseActivity() {
    private lateinit var binding: ActivityMicrophoneSensitivityBinding
    private var value = 2
    private var userAdjusted = false

    private val deviceUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothLeService.ACTION_UPDATE_LEVEL && !userAdjusted) {
                // The device value often arrives after this screen opens; re-seed
                // the picker as long as the user has not started adjusting it.
                seedFromDevice()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMicrophoneSensitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EdgeToEdgeUtils.handleRootWindowInsets(binding.root)
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        binding.microphoneSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) userAdjusted = true
                // TODO Auto-generated method stub
                binding.microphoneValue.text = progress.toString()
                value = progress
            }
        })

        binding.plus.setOnClickListener {
            userAdjusted = true
            if (value < 5) value += 1
            binding.microphoneSlider.progress = value
            binding.microphoneValue.text = "$value"
        }

        binding.minus.setOnClickListener {
            userAdjusted = true
            if (value > 1) value -= 1
            binding.microphoneSlider.progress = value
            binding.microphoneValue.text = "$value"
        }

        binding.microphoneConfirm.setOnClickListener {
            BluetoothLeService.service?.setMicrophone(value)
            finish()
        }

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

        seedFromDevice()

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

            val intent = Intent(this@MicrophoneSensitivityActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@MicrophoneSensitivityActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@MicrophoneSensitivityActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@MicrophoneSensitivityActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }

    private fun seedFromDevice() {
            with(BluetoothLeService.service?.device) {
                value = this?.microphoneLevel ?: 1
                binding.microphoneValue.text = value.toString()
                binding.microphoneSlider.progress = value
            }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(deviceUpdateReceiver, IntentFilter(BluetoothLeService.ACTION_UPDATE_LEVEL))
        if (!userAdjusted) seedFromDevice()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(deviceUpdateReceiver)
    }
}