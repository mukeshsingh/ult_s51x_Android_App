package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.ultrontech.s515liftconfigure.databinding.ActivityBoardDetailBinding

class BoardDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityBoardDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.llBtnEditJob.setOnClickListener {
            val intent = Intent(this, ChangeJobActivity::class.java)
            startActivity(intent)
        }

        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }

    fun updateWifiDetail() {
        with(bluetoothLeService) {
            if (device?.connectedSSID != null) {
                ssidConfiguredLabel.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.ssid_configured)
                wifiSsid.visibility = android.view.View.VISIBLE
                wifiSsid.text = device?.connectedSSID
            } else {
                ssidConfiguredLabel.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.ssid_not_configured)
                wifiSsid.visibility = android.view.View.GONE
            }

            if (device?.wifiAvailable == true) {
                wifiAvailableStatus.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.wifi_available_status)
                wifiAvailableStatus.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.white, theme))
            } else {
                wifiAvailableStatus.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.wifi_not_available_status)
                wifiAvailableStatus.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.custom_pink, theme))
            }

            if (device?.wifiConnected == true) {
                wifiConnectedStatus.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.wifi_connected)
                wifiConnectedStatus.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.white, theme))
            } else {
                wifiConnectedStatus.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.wifi_not_connected)
                wifiConnectedStatus.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.red, theme))
            }
        }
    }

    fun updateInfo() {
        with(bluetoothLeService) {
            if (device?.commsBoard != null) {
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 1u) {
                    capGSM.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.green_rounded_bg, theme)
                } else {
                    capGSM.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.grey_rounded_bg, theme)
                }
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 2u) {
                    capDiagnostics.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.green_rounded_bg, theme)
                } else {
                    capDiagnostics.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.grey_rounded_bg, theme)
                }
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 4u) {
                    capWifi.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.green_rounded_bg, theme)
                } else {
                    capWifi.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.grey_rounded_bg, theme)
                }

                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 8u) {
                    capWifiAP.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.green_rounded_bg, theme)
                } else {
                    capWifiAP.background = androidx.core.content.res.ResourcesCompat.getDrawable(resources, com.ultrontech.s515liftconfigure.R.drawable.grey_rounded_bg, theme)
                }
            }
        }
    }

    fun updateJob() {
        with(bluetoothLeService) {
            if (device?.job != null && device?.job?.length!! > 0) {
                jobLabel.visibility= android.view.View.VISIBLE
                job.text = device?.job
                jobLabel.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.job_name)
                job.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.white, theme))
            } else {
                jobLabel.visibility= android.view.View.GONE
                job.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.job_not_configured)
                job.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.custom_pink, theme))
            }

            if (device?.client != null && device?.client?.length!! > 0) {
                clientLabel.visibility= android.view.View.VISIBLE
                client.text = device?.client
                clientLabel.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.client)
                client.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.white, theme))
            } else {
                clientLabel.visibility= android.view.View.GONE
                client.text = resources.getString(com.ultrontech.s515liftconfigure.R.string.job_not_configured)
                client.setTextColor(resources.getColor(com.ultrontech.s515liftconfigure.R.color.custom_pink, theme))
            }
        }
    }
}