package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeWifiBinding

class ChangeWifiActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangeWifiBinding
    var security: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtWifiSsid.setText(BluetoothLeService.service?.device?.connectedSSID)

        binding.btnNoSecurity.setOnClickListener {
            security = false
            it.visibility = View.GONE
            binding.btnNoSecurityEnable.visibility = View.VISIBLE

            binding.btnWepWpa.visibility = View.GONE
            binding.btnWepWpaDisabled.visibility = View.VISIBLE

            binding.edtWifiPassword.visibility = View.GONE
            binding.edtWifiPassword.setText("")
        }
        binding.btnWepWpaDisabled.setOnClickListener {
            security = true
            it.visibility = View.GONE
            binding.btnWepWpa.visibility = View.VISIBLE

            binding.btnNoSecurity.visibility = View.VISIBLE
            binding.btnNoSecurityEnable.visibility = View.GONE

            binding.edtWifiPassword.visibility = View.VISIBLE
        }

        binding.btnConfirmWifi.setOnClickListener {
            BluetoothLeService.service?.setSSID(
                binding.edtWifiSsid.text.toString(),
                if (security) binding.edtWifiPassword.text.toString() else ""
            )

            finish()
        }

        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }
}