package com.ultrontech.s515liftconfigure

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeWifiPasswordBinding

class ChangeWifiPasswordActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivityChangeWifiPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeWifiPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConfirmWifi.setOnClickListener {
            ChangeWifiActivity.wifiPassword = binding.edtWifiPassword.text.toString()

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

            val intent = Intent(this@ChangeWifiPasswordActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeWifiPasswordActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeWifiPasswordActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@ChangeWifiPasswordActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }
}