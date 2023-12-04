package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeDialTimeoutBinding

class ChangeDialTimeoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeDialTimeoutBinding
    private var value = 5
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeDialTimeoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dialTimeoutSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO Auto-generated method stub
                binding.dialTimeoutValue.text = progress.toString()
                value = progress
            }
        })

        binding.dialTimeoutConfirm.setOnClickListener {
            BluetoothLeService.service?.setDialTimeout(value)
            finish()
        }

        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }

        with(BluetoothLeService.service?.device) {
            value = this?.callDialTimeout ?: 1
            binding.dialTimeoutValue.text = value.toString()
            binding.dialTimeoutSlider.progress = value
        }
    }
}