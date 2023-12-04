package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.setContact
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeUserContactBinding
import com.ultrontech.s515liftconfigure.models.PhoneContact

class ChangeUserContactActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangeUserContactBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeUserContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTextUserName.setText(name)
        binding.editTextUserPhone.setText(phone?.number)

        binding.btnConfirmUserDetail.setOnClickListener {
            with(BluetoothLeService.service) {
                this?.setContact(numberSlot, binding.editTextUserName.text.toString())
                val phone = binding.editTextUserPhone.text.toString().trim()
                if (phone.length >= 3) {
                    this?.setPhoneNumber(
                        numberSlot,
                        true,
                        phone
                    )

                    finish()
                } else {
                    this@ChangeUserContactActivity?.let { it1 ->
                        S515LiftConfigureApp.instance.basicAlert(
                            it1, "Please enter at least 3 digit phone number."
                        ){}
                    }
                }
            }
        }
        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, EngineerHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        binding.editTextUserName.setText(name)
        binding.editTextUserPhone.setText(phone?.number)
//        swtEnabled.isChecked = phone?.enabled == true
    }

    companion object {
        var numberSlot: Int = 1
        var phone: PhoneContact? = null
        var name: String? = ""
    }
}