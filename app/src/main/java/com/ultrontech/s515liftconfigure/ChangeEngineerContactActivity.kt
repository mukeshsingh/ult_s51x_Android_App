package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.setContact
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeEngineerContactBinding
import com.ultrontech.s515liftconfigure.models.PhoneContact

class ChangeEngineerContactActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangeEngineerContactBinding
    private var liftId: String? = null
    val numberSlot = 4
    var phone: PhoneContact? = null
    var name: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeEngineerContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)

        phone = BluetoothLeService.service?.device?.number4
        name = liftId?.let { it1 ->
            S515LiftConfigureApp.profileStore.find(
                it1
            )?.installerName
        }

        binding.edtEngineerName.setText(name)
        binding.edtEngineerPhone.setText(phone?.number)

        binding.btnConfirmEngineerContact.setOnClickListener {
            with(BluetoothLeService.service) {
                this?.setContact(numberSlot, binding.edtEngineerName.text.toString())
                val phone = binding.edtEngineerPhone.text.toString().trim()
                if (phone.length >= 3) {
                    this?.setPhoneNumber(
                        numberSlot,
                        true,
                        phone
                    )

                    finish()
                } else {
                    this@ChangeEngineerContactActivity?.let { it1 ->
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