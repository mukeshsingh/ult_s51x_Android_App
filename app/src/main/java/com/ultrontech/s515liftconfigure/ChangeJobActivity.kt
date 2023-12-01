package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeJobBinding

class ChangeJobActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangeJobBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val lift = BluetoothLeService.service?.device
        if (lift != null) {
            binding.edtJobName.setText(lift.job)
            binding.edtClientName.setText(lift.client)
        }

        binding.btnConfirmJobName.setOnClickListener {
            BluetoothLeService.service?.setJob(
                binding.edtJobName.text.toString(),
                binding.edtClientName.text.toString()
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