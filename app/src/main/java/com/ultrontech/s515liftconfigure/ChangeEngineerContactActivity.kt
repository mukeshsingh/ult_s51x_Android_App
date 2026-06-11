package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsetsController
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.setContact
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeEngineerContactBinding
import com.ultrontech.s515liftconfigure.models.PhoneContact
import com.ultrontech.s515liftconfigure.util.EdgeToEdgeUtils

class ChangeEngineerContactActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivityChangeEngineerContactBinding
    private var liftId: String? = null
    val numberSlot = 4
    var phone: PhoneContact? = null
    private var seeding = false
    private var userEdited = false

    private val deviceUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothLeService.ACTION_UPDATE_PHONE_SLOT,
                BluetoothLeService.ACTION_CLEAR_PHONE_SLOT -> {
                    // The slot data often arrives after this screen opens; re-seed the
                    // fields as long as the user has not started editing - otherwise a
                    // confirm on the empty form wipes the configured contact.
                    if (!userEdited) seedFromDevice()
                }
            }
        }
    }

    private val editWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (!seeding) userEdited = true
        }
    }

    private fun seedFromDevice() {
        phone = BluetoothLeService.service?.device?.number4
        seeding = true
        binding.edtEngineerName.setText(phone?.contactName)
        binding.edtEngineerPhone.setText(phone?.number)
        seeding = false
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_UPDATE_PHONE_SLOT)
            addAction(BluetoothLeService.ACTION_CLEAR_PHONE_SLOT)
        }
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(deviceUpdateReceiver, filter)
        if (!userEdited) seedFromDevice()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(deviceUpdateReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeEngineerContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EdgeToEdgeUtils.handleRootWindowInsets(binding.root)
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)

        seedFromDevice()
        binding.edtEngineerName.addTextChangedListener(editWatcher)
        binding.edtEngineerPhone.addTextChangedListener(editWatcher)

        binding.btnConfirmEngineerContact.setOnClickListener {
            with(BluetoothLeService.service) {
                this?.setContact(numberSlot, binding.edtEngineerName.text.toString())
                val phone = binding.edtEngineerPhone.text.toString().trim()
                val name = binding.edtEngineerName.text.toString().trim()

                this?.setPhoneNumber(
                    numberSlot,
                    true,
                    phone,
                    name
                )

                finish()
            }
        }
        binding.footer.btnHome.setOnClickListener {
            finish()
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

            val intent = Intent(this@ChangeEngineerContactActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeEngineerContactActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeEngineerContactActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@ChangeEngineerContactActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }
}