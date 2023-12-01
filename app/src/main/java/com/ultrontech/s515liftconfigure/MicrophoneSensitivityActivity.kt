package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeVolumeBinding
import com.ultrontech.s515liftconfigure.databinding.ActivityMicrophoneSensitivityBinding

class MicrophoneSensitivityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMicrophoneSensitivityBinding
    private var value = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMicrophoneSensitivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.microphoneSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO Auto-generated method stub
                binding.microphoneValue.text = progress.toString()
                value = progress
            }
        })

        binding.microphoneConfirm.setOnClickListener {
            BluetoothLeService.service?.setMicrophone(value)
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

        with(BluetoothLeService.service?.device) {
            value = this?.microphoneLevel ?: 1
            binding.microphoneValue.text = value.toString()
            binding.microphoneSlider.progress = value
        }
    }
}