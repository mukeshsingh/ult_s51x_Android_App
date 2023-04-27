package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.fragments.*
import com.ultrontech.s515liftconfigure.models.*

class EngineerDetailsActivity : AppCompatActivity() {
    private lateinit var liftName: TextView
    private lateinit var deviceStatus: TextView
    private lateinit var btnEdit: Button
    private lateinit var btnConnect: Button
    private lateinit var btnRemove: Button

    private lateinit var liftModel: TextView
    private lateinit var ssidConfiguredLabel: TextView
    private lateinit var wifiSsid: TextView
    private lateinit var wifiAvailableStatus: TextView
    private lateinit var wifiConnectedStatus: TextView

    private lateinit var jobLabel: TextView
    private lateinit var job: TextView
    private lateinit var clientLabel: TextView
    private lateinit var client: TextView

    private lateinit var capGSM: TextView
    private lateinit var capDiagnostics: TextView
    private lateinit var capWifi: TextView
    private lateinit var capWifiAP: TextView
    private lateinit var btnBoardEdit: Button
    private lateinit var btnEditContact1: Button
    private lateinit var btnEditContact2: Button
    private lateinit var btnEditContact3: Button
    private lateinit var btnEditContact4: Button
    private lateinit var btnEditContact5: Button
    private lateinit var btnDial: Button
    private lateinit var btnCallPress: Button
    private lateinit var btnVolume: Button
    private lateinit var btnMicrophone: Button
    private lateinit var btnEditSim: Button

    private val bluetoothLeService: BluetoothLeService = BluetoothLeService.service!!

    private val bottomSheetEditLiftFrag = EditLiftFragment()
    private val editBoardDetailsFragment = EditBoardDetailsFragment()
    private val editSimFragment = EditSimFragment()
    private val editVolumeFragment = EditVolumeFragment()
    private val editCallDelayFragment = EditCallDelayFragment()
    private val editContactFragment = EditContactFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_engineer_details)

        liftName = findViewById(R.id.txt_lift_name)
        deviceStatus = findViewById(R.id.txt_device_status)
        btnConnect = findViewById(R.id.btn_connect)
        btnEdit = findViewById(R.id.btn_edit)
        btnRemove = findViewById(R.id.btn_remove)

        liftModel = findViewById(R.id.txt_lift_model)
        ssidConfiguredLabel = findViewById(R.id.txt_ssid_configured_label)
        wifiSsid = findViewById(R.id.txt_ssid)
        wifiAvailableStatus = findViewById(R.id.txt_wifi_available_status)
        wifiConnectedStatus= findViewById(R.id.txt_wifi_connected_status)

        job = findViewById(R.id.txt_job)
        jobLabel = findViewById(R.id.txt_job_label)
        client = findViewById(R.id.txt_client)
        clientLabel = findViewById(R.id.txt_client_label)

        capGSM = findViewById(R.id.txt_cap_gsm)
        capDiagnostics = findViewById(R.id.txt_cap_diagnostics)
        capWifi = findViewById(R.id.txt_cap_wifi)
        capWifiAP = findViewById(R.id.txt_cap_wifi_ap)

        btnBoardEdit = findViewById(R.id.btn_board_edit)
        btnEditContact1 = findViewById(R.id.btn_edit_contact1)
        btnEditContact2 = findViewById(R.id.btn_edit_contact2)
        btnEditContact3 = findViewById(R.id.btn_edit_contact3)
        btnEditContact4 = findViewById(R.id.btn_edit_contact4)
        btnEditContact5 = findViewById(R.id.btn_edit_contact5)
        btnDial = findViewById(R.id.btn_dial)
        btnCallPress = findViewById(R.id.btn_call_press)
        btnVolume = findViewById(R.id.btn_volume)
        btnMicrophone = findViewById(R.id.btn_microphone)
        btnEditSim = findViewById(R.id.btn_sim_edit)

        btnRemove.setOnClickListener {
            bluetoothLeService.device.lift?.let { it1 ->
                S515LiftConfigureApp.profileStore.remove(it1)
                finish()
            }
        }
        btnEdit.setOnClickListener {
            bottomSheetEditLiftFrag.show(supportFragmentManager, "bottomSheetEditLiftFrag")
        }

        btnEditSim.setOnClickListener {
            editSimFragment.show(supportFragmentManager, "editSimFragment")
        }

        btnBoardEdit.setOnClickListener {
            editBoardDetailsFragment.show(supportFragmentManager, "editBoardDetailsFragment")
        }

        btnEditContact1.setOnClickListener {
            editContactFragment.show(supportFragmentManager, "editContactFragment")
        }

        btnCallPress.setOnClickListener {
            editCallDelayFragment.show(supportFragmentManager, "editCallDelayFragment")
        }

        btnVolume.setOnClickListener {
            editVolumeFragment.show(supportFragmentManager, "editVolumeFragment")
        }
    }

    private fun linkDevice () {
        val liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)
        if (liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(liftId)
            var device = Device(lift = lift)
            bluetoothLeService.link(device)

            liftName.text = lift?.liftName ?: ""
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(deviceUpdateReceiver, updateIntentFilter())

        linkDevice()
    }

    override fun onPause() {
        unregisterReceiver(deviceUpdateReceiver)
        super.onPause()
    }

    private fun updateConnectState() {
        return
        with(bluetoothLeService) {
            when(device.connectionState) {
                LiftConnectionState.connected_noauth -> {
                    deviceStatus.text = resources.getString(R.string.device_connected_no_auth)
                    btnConnect.visibility = View.GONE
                    btnEdit.visibility = View.GONE
                    device.lift?.let { bluetoothLeService.authorise(it) }
                }
                LiftConnectionState.connected_auth -> {
                    deviceStatus.text = resources.getString(R.string.device_connected)
                    btnConnect.visibility = View.GONE
                    btnEdit.visibility = View.VISIBLE
                }
                LiftConnectionState.not_connected -> {
                    deviceStatus.text = resources.getString(R.string.device_not_connected)
                    btnConnect.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                }
                LiftConnectionState.connect_error -> {
                    deviceStatus.text = resources.getString(R.string.device_connect_error)
                    btnConnect.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                }
            }
        }
    }

    fun updateWifiDetail() {
        with(bluetoothLeService) {
            if (device.connectedSSID != null) {
                ssidConfiguredLabel.text = resources.getString(R.string.ssid_configured)
                wifiSsid.visibility = View.VISIBLE
                wifiSsid.text = device.connectedSSID
            } else {
                ssidConfiguredLabel.text = resources.getString(R.string.ssid_not_configured)
                wifiSsid.visibility = View.GONE
            }

            if (device.wifiAvailable) {
                wifiAvailableStatus.text = resources.getString(R.string.wifi_available_status)
            } else {
                wifiAvailableStatus.text = resources.getString(R.string.wifi_not_available_status)
            }

            if (device.wifiConnected) {
                wifiConnectedStatus.text = resources.getString(R.string.wifi_connected)
                wifiConnectedStatus.setTextColor(resources.getColor(R.color.white, theme))
            } else {
                wifiConnectedStatus.text = resources.getString(R.string.wifi_not_connected)
                wifiConnectedStatus.setTextColor(resources.getColor(R.color.red, theme))
            }
        }
    }

    fun updateInfo() {
        with(bluetoothLeService) {
            if (device.commsBoard != null) {
                if (device.commsBoard!!.capabilities.getAll()[0].rawValue == 1u) {
                    capGSM.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capGSM.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }
                if (device.commsBoard!!.capabilities.getAll()[0].rawValue == 2u) {
                    capDiagnostics.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capDiagnostics.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }
                if (device.commsBoard!!.capabilities.getAll()[0].rawValue == 4u) {
                    capWifi.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capWifi.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }

                if (device.commsBoard!!.capabilities.getAll()[0].rawValue == 8u) {
                    capWifiAP.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capWifiAP.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }
            }
        }
    }

    fun updateJob() {
        with(bluetoothLeService) {
            if (device.job != null) {
                jobLabel.visibility= View.VISIBLE
                job.text = device.job
                jobLabel.text = resources.getString(R.string.job_name)
            } else {
                jobLabel.visibility= View.GONE
                job.text = resources.getString(R.string.job_not_configured)
            }

            if (device.client != null) {
                clientLabel.visibility= View.VISIBLE
                client.text = device.client
                clientLabel.text = resources.getString(R.string.client)
            } else {
                clientLabel.visibility= View.GONE
                client.text = resources.getString(R.string.job_not_configured)
            }
        }
    }

    private val deviceUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_CONNECTION_UPDATE -> {
                    Log.d(HomeActivity.TAG, "Device connecting.")

                    updateConnectState()
                }
                BluetoothLeService.ACTION_UPDATE_INFO -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_INFO.")

                    updateInfo()
                }
                BluetoothLeService.ACTION_UPDATE_LEVEL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_LEVEL.")
                }
                BluetoothLeService.ACTION_UPDATE_AUTHENTICATION -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_AUTHENTICATION.")
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_SLOT.")
                }
                BluetoothLeService.ACTION_CLEAR_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_CLEAR_PHONE_SLOT.")
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_CONFIG.")
                }
                BluetoothLeService.ACTION_UPDATE_JOB -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_JOB.")
                    updateJob()
                }
                BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_WIFI_DETAIL.")
                    updateWifiDetail()
                }
                BluetoothLeService.ACTION_UPDATE_SSID_LIST -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_SSID_LIST.")
                }
                BluetoothLeService.ACTION_BLUETOOTH_ON -> {
                    Log.d(HomeActivity.TAG, "ACTION_BLUETOOTH_ON.")
                }
                BluetoothLeService.ACTION_BLUETOOTH_OFF -> {
                    finish()
                }
                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    bluetoothLeService?.updateServices()
                }
            }
        }
    }
    private fun updateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_BLUETOOTH_ON)
            addAction(BluetoothLeService.ACTION_BLUETOOTH_OFF)
            addAction(BluetoothLeService.ACTION_CONNECTION_UPDATE)
            addAction(BluetoothLeService.ACTION_UPDATE_SSID_LIST)
            addAction(BluetoothLeService.ACTION_UPDATE_JOB)
            addAction(BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG)
            addAction(BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL)
            addAction(BluetoothLeService.ACTION_UPDATE_AUTHENTICATION)
            addAction(BluetoothLeService.ACTION_UPDATE_PHONE_SLOT)
            addAction(BluetoothLeService.ACTION_UPDATE_INFO)
            addAction(BluetoothLeService.ACTION_UPDATE_LEVEL)
            addAction(BluetoothLeService.ACTION_CLEAR_PHONE_SLOT)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        }
    }
}