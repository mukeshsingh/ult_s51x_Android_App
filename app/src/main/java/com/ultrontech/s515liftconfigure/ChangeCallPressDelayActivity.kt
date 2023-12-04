package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeCallPressDelayBinding

class ChangeCallPressDelayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeCallPressDelayBinding
    private var value = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeCallPressDelayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.callPressSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO Auto-generated method stub
                binding.callPressValue.text = progress.toString()
                value = progress
            }
        })

        binding.callPressConfirm.setOnClickListener {
            BluetoothLeService.service?.setPressDelay(value)
            finish()
        }

        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }

        with(BluetoothLeService.service?.device) {
            value = this?.callPressDelay ?: 1
            binding.callPressValue.text = value.toString()
            binding.callPressSlider.progress = value
        }
    }
}