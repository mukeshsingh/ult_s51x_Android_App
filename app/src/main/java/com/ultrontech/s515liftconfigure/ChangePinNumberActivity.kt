package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.setAccess
import com.ultrontech.s515liftconfigure.databinding.ActivityChangePinNumberBinding
import com.ultrontech.s515liftconfigure.models.PINNumber

class ChangePinNumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePinNumberBinding
    private var liftId: String? = null
    private lateinit var newPin: EditText
    private lateinit var confirmPin: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePinNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)
        newPin = binding.editTextNewPin
        confirmPin = binding.editTextConfirmPin

        if (liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(liftId!!)
            Log.e("", ">>>>>>>>>>>>>>>> ${lift?.liftName ?: ""}")
        }

        binding.btnConfirmSimPin .setOnClickListener {
            var pinStr = newPin.text.toString().trim()
            var confirmPinStr = confirmPin.text.toString().trim()

            if (pinStr == confirmPinStr && newPin.text.toString().trim().length == 6) {
                val accessKey = pinStr.map { it.digitToInt() }.toIntArray()

                BluetoothLeService.service?.setAccess(PINNumber(6, accessKey))
                newPin.setText("")
                confirmPin.setText("")
                finish()
            } else if (pinStr == confirmPinStr) {
                Snackbar.make(it, "Pin length must be 6 digit.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            } else {
                Snackbar.make(it, "New Pin and Confirm Pin must be match.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
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

            val intent = Intent(this@ChangePinNumberActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangePinNumberActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangePinNumberActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@ChangePinNumberActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }
}