package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityBoardDetailBinding

class BoardDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityBoardDetailBinding
    private val bluetoothLeService: BluetoothLeService = BluetoothLeService.service!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.llBtnEditJob.setOnClickListener {
            val intent = Intent(this, ChangeJobActivity::class.java)
            startActivity(intent)
        }

        binding.llBtnEditWifi.setOnClickListener {
            val intent = Intent(this, ChangeWifiActivity::class.java)
            startActivity(intent)
        }

        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }

        updateWifiDetail()
        updateInfo()
        updateJob()
    }

    private fun updateWifiDetail() {
        with(bluetoothLeService) {
            if (device?.connectedSSID != null) {
                binding.ssidConfiguredLabel.text = resources.getString(R.string.ssid_configured)
                binding.ssidConfiguredLabel.setTextColor(resources.getColor(R.color.text_color_title, theme))
            } else {
                binding.ssidConfiguredLabel.text = resources.getString(R.string.ssid_not_configured)
                binding.ssidConfiguredLabel.setTextColor(resources.getColor(R.color.dark_red, theme))
            }

            if (device?.wifiAvailable == true) {
                binding.wifiAvailableStatus.text = resources.getString(R.string.wifi_available_status)
                binding.wifiAvailableStatus.setTextColor(resources.getColor(R.color.text_color_title, theme))
            } else {
                binding.wifiAvailableStatus.text = resources.getString(R.string.wifi_not_available_status)
                binding.wifiAvailableStatus.setTextColor(resources.getColor(R.color.red, theme))
            }

            if (device?.wifiConnected == true) {
                binding.wifiConnectedStatus.text = resources.getString(R.string.wifi_connected)
                binding.wifiConnectedStatus.setTextColor(resources.getColor(R.color.text_color_title, theme))
            } else {
                binding.wifiConnectedStatus.text = resources.getString(R.string.wifi_not_connected)
                binding.wifiConnectedStatus.setTextColor(resources.getColor(R.color.red, theme))
            }
        }
    }

    private fun updateInfo() {
        with(bluetoothLeService) {
            if (device?.commsBoard != null) {
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 1u) {
                    binding.capGSM.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_green, theme)
                } else {
                    binding.capGSM.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_red, theme)
                }
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 2u) {
                    binding.capDiagnostics.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_green, theme)
                } else {
                    binding.capDiagnostics.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_red, theme)
                }
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 4u) {
                    binding.capWifi.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_green, theme)
                } else {
                    binding.capWifi.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_red, theme)
                }

                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 8u) {
                    binding.capWifiAP.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_green, theme)
                } else {
                    binding.capWifiAP.background = ResourcesCompat.getDrawable(resources, R.drawable.circle_bullet_red, theme)
                }
            }
        }
    }

    private fun updateJob() {
        with(bluetoothLeService) {
            if (device?.job != null && device?.job?.length!! > 0) {
                binding.jobLabel.visibility = View.VISIBLE
                binding.job.text = device?.job
                binding.jobLabel.text = resources.getString(R.string.job_name)
            } else {
                binding.jobLabel.visibility = View.GONE
                binding.job.text = resources.getString(R.string.job_not_configured)
                binding.job.setTextColor(resources.getColor(R.color.dark_red, theme))
            }

            if (device?.client != null && device?.client?.length!! > 0) {
                binding.clientLabel.visibility = View.VISIBLE
                binding.client.text = device?.client
                binding.clientLabel.text = resources.getString(R.string.client)
            } else {
                binding.clientLabel.visibility = View.GONE
                binding.client.text = resources.getString(R.string.job_not_configured)
                binding.client.setTextColor(resources.getColor(R.color.dark_red, theme))
            }
        }
    }
}