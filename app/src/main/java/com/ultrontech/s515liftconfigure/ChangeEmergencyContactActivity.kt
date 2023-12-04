package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeEmergencyContactBinding
import com.ultrontech.s515liftconfigure.models.PhoneContact

class ChangeEmergencyContactActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangeEmergencyContactBinding
    private var liftId: String? = null
    var numberSlot = 5
    var phone: PhoneContact? = null
    var name: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeEmergencyContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                        phone
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
    }
}