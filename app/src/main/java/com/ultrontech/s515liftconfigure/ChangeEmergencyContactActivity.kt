package com.ultrontech.s515liftconfigure

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeEmergencyContactBinding
import com.ultrontech.s515liftconfigure.models.PhoneContact
import com.ultrontech.s515liftconfigure.util.EdgeToEdgeUtils

class ChangeEmergencyContactActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivityChangeEmergencyContactBinding
    private var liftId: String? = null
    var numberSlot = 5
    var phone: PhoneContact? = null
    var name: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeEmergencyContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EdgeToEdgeUtils.handleRootWindowInsets(binding.root)
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        phone = BluetoothLeService.service?.device?.number5
        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)
        name = liftId?.let { it1 ->
            S515LiftConfigureApp.profileStore.find(
                it1
            )?.emergencyName
        }

        binding.edtEmergencyPhone.setText(phone?.number)

        binding.btnConfirmEmergency.setOnClickListener {
            with(BluetoothLeService.service) {
                val phone = binding.edtEmergencyPhone.text.toString().trim()
                if (phone.length >= 3) {
                    this?.setPhoneNumber(
                        numberSlot,
                        true,
                        phone,
                        ""
                    )

                    finish()
                } else {
                    this@ChangeEmergencyContactActivity?.let { it1 ->
                        S515LiftConfigureApp.instance.basicAlert(
                            it1, "Please enter at least 3 digit phone number."
                        ){}
                    }
                }
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

            val intent = Intent(this@ChangeEmergencyContactActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeEmergencyContactActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeEmergencyContactActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@ChangeEmergencyContactActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }
}