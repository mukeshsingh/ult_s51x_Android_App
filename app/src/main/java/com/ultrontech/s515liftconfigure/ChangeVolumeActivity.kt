package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeVolumeBinding

class ChangeVolumeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeVolumeBinding
    private var value = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeVolumeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.volumeSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO Auto-generated method stub
                binding.volumeValue.text = progress.toString()
                value = progress
            }
        })

        binding.volumeConfirm.setOnClickListener {
            BluetoothLeService.service?.setVolume(value)
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
            value = this?.volumeLevel ?: 1
            binding.volumeValue.text = value.toString()
            binding.volumeSlider.progress = value
        }
    }
}