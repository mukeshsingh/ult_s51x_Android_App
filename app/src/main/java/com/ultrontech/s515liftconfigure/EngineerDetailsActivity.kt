package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Image
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
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

    private lateinit var img1: Image
    private lateinit var img2: Image
    private lateinit var img3: Image
    private lateinit var img4: Image
    private lateinit var img5: Image

    private lateinit var contactName1: TextView
    private lateinit var contactName2: TextView
    private lateinit var contactName3: TextView
    private lateinit var contactName4: TextView
    private lateinit var contactName5: TextView

    private lateinit var phoneNumber1: TextView
    private lateinit var phoneNumber2: TextView
    private lateinit var phoneNumber3: TextView
    private lateinit var phoneNumber4: TextView
    private lateinit var phoneNumber5: TextView

    private lateinit var callCount1: TextView
    private lateinit var callCount2: TextView
    private lateinit var callCount3: TextView
    private lateinit var callCount4: TextView
    private lateinit var callCount5: TextView

    private lateinit var lastDialed1: TextView
    private lateinit var lastDialed2: TextView
    private lateinit var lastDialed3: TextView
    private lateinit var lastDialed4: TextView
    private lateinit var lastDialed5: TextView

    private lateinit var lastVoice1: TextView
    private lateinit var lastVoice2: TextView
    private lateinit var lastVoice3: TextView
    private lateinit var lastVoice4: TextView
    private lateinit var lastVoice5: TextView

    private lateinit var disabled1: TextView
    private lateinit var disabled2: TextView
    private lateinit var disabled3: TextView
    private lateinit var disabled4: TextView
    private lateinit var disabled5: TextView

    private lateinit var dialedTimeout: TextView
    private lateinit var callPressDelay: TextView
    private lateinit var volume: TextView
    private lateinit var microphone: TextView

    private val bluetoothLeService: BluetoothLeService = BluetoothLeService.service!!

    private val bottomSheetEditLiftFrag = EditLiftFragment()
    private val editBoardDetailsFragment = EditBoardDetailFragment()
    private val editSimFragment = EditSimFragment()
    private val editVolumeFragment = EditVolumeFragment()
    private val editCallDelayFragment = EditCallDelayFragment()
    private val editContactFragment = EditContactFragment()
    private val editDialTimeoutFragment = EditDialTimeoutFragment()
    private val editMicrophoneFragment = EditMicrophoneFragment()

    private lateinit var simType: TextView
    private lateinit var txtMessage: TextView
    private lateinit var txtPin: TextView
    private lateinit var txtPinLabel: TextView
    private var liftId: String? = null

    private lateinit var engineerDetailContainer:CardView
    private lateinit var simInfoContainer:CardView
    private lateinit var userContactContainer:CardView
    private lateinit var dialTimeContainer:CardView
    private lateinit var callPressContainer:CardView
    private lateinit var volumeContainer:CardView
    private lateinit var microphoneContainer:CardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_engineer_details)

        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)

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

        simType = findViewById(R.id.txtSimType)
        txtPin = findViewById(R.id.txtCurrentPin)
        txtMessage = findViewById(R.id.txtMessage)
        txtPinLabel = findViewById(R.id.txtCurrentPinLabel)

        contactName1 = findViewById(R.id.txtContactName1)
        contactName2 = findViewById(R.id.txtContactName2)
        contactName3 = findViewById(R.id.txtContactName3)
        contactName4 = findViewById(R.id.txtContactName4)
        contactName5 = findViewById(R.id.txtContactName5)

        phoneNumber1 = findViewById(R.id.txtPhoneNumber1)
        phoneNumber2 = findViewById(R.id.txtPhoneNumber2)
        phoneNumber3 = findViewById(R.id.txtPhoneNumber3)
        phoneNumber4 = findViewById(R.id.txtPhoneNumber4)
        phoneNumber5 = findViewById(R.id.txtPhoneNumber5)

        callCount1 = findViewById(R.id.txtCallCount1)
        callCount2 = findViewById(R.id.txtCallCount2)
        callCount3 = findViewById(R.id.txtCallCount3)
        callCount4 = findViewById(R.id.txtCallCount4)
        callCount5 = findViewById(R.id.txtCallCount5)

        lastDialed1 = findViewById(R.id.txtLastDialed1)
        lastDialed2 = findViewById(R.id.txtLastDialed2)
        lastDialed3 = findViewById(R.id.txtLastDialed3)
        lastDialed4 = findViewById(R.id.txtLastDialed4)
        lastDialed5 = findViewById(R.id.txtLastDialed5)

        lastVoice1 = findViewById(R.id.txtLastVoice1)
        lastVoice2 = findViewById(R.id.txtLastVoice2)
        lastVoice3 = findViewById(R.id.txtLastVoice3)
        lastVoice4 = findViewById(R.id.txtLastVoice4)
        lastVoice5 = findViewById(R.id.txtLastVoice5)

        dialedTimeout = findViewById(R.id.txtDialTimeout)
        callPressDelay = findViewById(R.id.txtCallPressDelay)
        volume = findViewById(R.id.txtVolume)
        microphone = findViewById(R.id.txtMicrophone)

        engineerDetailContainer = findViewById(R.id.engineerDetailContainer)
        simInfoContainer = findViewById(R.id.simInfoContainer)
        userContactContainer = findViewById(R.id.userContactContainer)
        dialTimeContainer = findViewById(R.id.dialTimeContainer)
        callPressContainer = findViewById(R.id.callPressContainer)
        volumeContainer = findViewById(R.id.volumeContainer)
        microphoneContainer = findViewById(R.id.microphoneContainer)

        btnRemove.setOnClickListener {
            bluetoothLeService.device?.lift?.let { it1 ->
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
            editContactFragment.numberSlot = 1
            editContactFragment.phone = BluetoothLeService.service?.device?.number1
            editContactFragment.name = liftId?.let { it1 ->
                S515LiftConfigureApp.profileStore.find(
                    it1
                )?.userContact1Name
            }
            editContactFragment.show(supportFragmentManager, "editContactFragment")
        }

        btnEditContact2.setOnClickListener {
            editContactFragment.numberSlot = 2
            editContactFragment.phone = BluetoothLeService.service?.device?.number2
            editContactFragment.name = liftId?.let { it1 ->
                S515LiftConfigureApp.profileStore.find(
                    it1
                )?.userContact2Name
            }
            editContactFragment.show(supportFragmentManager, "editContactFragment")
        }

        btnEditContact3.setOnClickListener {
            editContactFragment.numberSlot = 3
            editContactFragment.phone = BluetoothLeService.service?.device?.number3
            editContactFragment.name = liftId?.let { it1 ->
                S515LiftConfigureApp.profileStore.find(
                    it1
                )?.userContact3Name
            }
            editContactFragment.show(supportFragmentManager, "editContactFragment")
        }

        btnEditContact4.setOnClickListener {
            editContactFragment.numberSlot = 4
            editContactFragment.phone = BluetoothLeService.service?.device?.number4
            editContactFragment.name = liftId?.let { it1 ->
                S515LiftConfigureApp.profileStore.find(
                    it1
                )?.installerName
            }
            editContactFragment.show(supportFragmentManager, "editContactFragment")
        }

        btnEditContact5.setOnClickListener {
            editContactFragment.numberSlot = 5
            editContactFragment.phone = BluetoothLeService.service?.device?.number5
            editContactFragment.name = liftId?.let { it1 ->
                S515LiftConfigureApp.profileStore.find(
                    it1
                )?.emergencyName
            }
            editContactFragment.show(supportFragmentManager, "editContactFragment")
        }

        btnCallPress.setOnClickListener {
            editCallDelayFragment.show(supportFragmentManager, "editCallDelayFragment")
        }

        btnVolume.setOnClickListener {
            editVolumeFragment.show(supportFragmentManager, "editVolumeFragment")
        }

        btnDial.setOnClickListener {
            editDialTimeoutFragment.show(supportFragmentManager, "editDialTimeoutFragment")
        }

        btnMicrophone.setOnClickListener {
            editMicrophoneFragment.show(supportFragmentManager, "editMicrophoneFragment")
        }

        btnConnect.setOnClickListener {
            linkDevice()
        }
    }

    private fun linkDevice () {
        if (liftId != null) {
            val lift = S515LiftConfigureApp.profileStore.find(liftId!!)
            if (lift != null) {
                var device = Device(lift = lift)
                bluetoothLeService.link(device)

                liftName.text = lift?.liftName ?: ""
            }
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

    private fun showHideCards(visibility: Int){
        engineerDetailContainer.visibility = visibility
        simInfoContainer.visibility = visibility
        userContactContainer.visibility = visibility
        dialTimeContainer.visibility = visibility
        callPressContainer.visibility = visibility
        volumeContainer.visibility = visibility
        microphoneContainer.visibility = visibility
    }
    private fun updateConnectState() {
        with(bluetoothLeService) {
            when(device?.connectionState) {
                LiftConnectionState.connected_noauth -> {
                    deviceStatus.text = resources.getString(R.string.device_connected_no_auth)
                    btnConnect.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                    showHideCards(View.GONE)
                    device?.lift?.let { bluetoothLeService.authorise(it) }
                }
                LiftConnectionState.connected_auth -> {
                    deviceStatus.text = resources.getString(R.string.device_connected)
                    btnConnect.visibility = View.GONE
                    btnEdit.visibility = View.VISIBLE
                    showHideCards(View.VISIBLE)
                }
                LiftConnectionState.not_connected -> {
                    deviceStatus.text = resources.getString(R.string.device_not_connected)
                    btnConnect.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                    showHideCards(View.GONE)
                }
                LiftConnectionState.connect_error -> {
                    deviceStatus.text = resources.getString(R.string.device_connect_error)
                    btnConnect.visibility = View.VISIBLE
                    btnEdit.visibility = View.GONE
                    showHideCards(View.GONE)
                }
                else -> {}
            }
        }
    }

    fun updateWifiDetail() {
        with(bluetoothLeService) {
            if (device?.connectedSSID != null) {
                ssidConfiguredLabel.text = resources.getString(R.string.ssid_configured)
                wifiSsid.visibility = View.VISIBLE
                wifiSsid.text = device?.connectedSSID
            } else {
                ssidConfiguredLabel.text = resources.getString(R.string.ssid_not_configured)
                wifiSsid.visibility = View.GONE
            }

            if (device?.wifiAvailable == true) {
                wifiAvailableStatus.text = resources.getString(R.string.wifi_available_status)
                wifiAvailableStatus.setTextColor(resources.getColor(R.color.white, theme))
            } else {
                wifiAvailableStatus.text = resources.getString(R.string.wifi_not_available_status)
                wifiAvailableStatus.setTextColor(resources.getColor(R.color.custom_pink, theme))
            }

            if (device?.wifiConnected == true) {
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
            if (device?.commsBoard != null) {
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 1u) {
                    capGSM.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capGSM.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 2u) {
                    capDiagnostics.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capDiagnostics.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }
                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 4u) {
                    capWifi.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capWifi.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }

                if (device?.commsBoard!!.capabilities.getAll()[0].rawValue == 8u) {
                    capWifiAP.background = ResourcesCompat.getDrawable(resources, R.drawable.green_rounded_bg, theme)
                } else {
                    capWifiAP.background = ResourcesCompat.getDrawable(resources, R.drawable.grey_rounded_bg, theme)
                }
            }
        }
    }

    fun updateJob() {
        with(bluetoothLeService) {
            if (device?.job != null && device?.job?.length!! > 0) {
                jobLabel.visibility= View.VISIBLE
                job.text = device?.job
                jobLabel.text = resources.getString(R.string.job_name)
                job.setTextColor(resources.getColor(R.color.white, theme))
            } else {
                jobLabel.visibility= View.GONE
                job.text = resources.getString(R.string.job_not_configured)
                job.setTextColor(resources.getColor(R.color.custom_pink, theme))
            }

            if (device?.client != null && device?.client?.length!! > 0) {
                clientLabel.visibility= View.VISIBLE
                client.text = device?.client
                clientLabel.text = resources.getString(R.string.client)
                client.setTextColor(resources.getColor(R.color.white, theme))
            } else {
                clientLabel.visibility= View.GONE
                client.text = resources.getString(R.string.job_not_configured)
                client.setTextColor(resources.getColor(R.color.custom_pink, theme))
            }
        }
    }

    private fun updatePhoneConfig() {
        with(bluetoothLeService) {
            if (device?.simType != null) {
                simType.text = Util.getSimTypeName(device?.simType!!)
            }

            if (device?.simPin?.pin != null) {
                txtPinLabel.text = resources.getString(R.string.pin_number_configured)
                txtPin.visibility = View.VISIBLE
                txtPin.text = device?.simPin?.pin?.display()
            } else {
                txtPin.visibility = View.GONE
                txtPinLabel.text = resources.getString(R.string.no_pin_number_configured)
            }

            if (device?.noPreviousCalls() == true) {
                txtMessage.text = resources.getString(R.string.you_have_made_no_calls)
            } else if (device?.callReminderNeeded() == true) {
                txtMessage.text = resources.getString(R.string.call_reminder_needed)
            }

            callPressDelay.text = (device?.callPressDelay ?: "??").toString()
            dialedTimeout.text = (device?.callDialTimeout ?: "??").toString()
        }
    }

    private fun updatePhoneSlots() {
        for(i in 1..5) {
            with(BluetoothLeService.service) {
                when (i) {
                    1 -> updatePhoneSlot1(this?.device?.number1, this?.device?.lift?.userContact1Name)
                    2 -> updatePhoneSlot2(this?.device?.number1, this?.device?.lift?.userContact1Name)
                    3 -> updatePhoneSlot3(this?.device?.number1, this?.device?.lift?.userContact1Name)
                    4 -> updatePhoneSlot4(this?.device?.number1, this?.device?.lift?.userContact1Name)
                    5 -> updatePhoneSlot4(this?.device?.number1, this?.device?.lift?.userContact1Name)
                }
            }
        }
    }

    private fun updatePhoneSlot1(phone: PhoneContact?, contactName: String?) {
        if (phone?.populated == true) {
            phoneNumber1.visibility = View.VISIBLE
            callCount1.visibility = View.VISIBLE
            lastDialed1.visibility = View.VISIBLE
            lastVoice1.visibility = View.VISIBLE

            contactName1.text = contactName
            phoneNumber1.text = phone?.number

            if (phone?.callCount != null) {
                callCount1.text = "${phone?.callCount} calls made"
            } else {
                callCount1.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastDialled != null) {
                lastDialed1.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastDialed1.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastDialled != null) {
                lastVoice1.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastVoice1.text = resources.getString(R.string.callmade)
            }
        } else {
            contactName1.text = resources.getString(R.string.missing_number)
            phoneNumber1.visibility = View.GONE
            callCount1.visibility = View.GONE
            lastDialed1.visibility = View.GONE
            lastVoice1.visibility = View.GONE
        }

        if (phone?.enabled == true) {
            disabled1.visibility = View.GONE
        } else {
            disabled1.visibility = View.VISIBLE
        }
    }

    private fun updatePhoneSlot2(phone: PhoneContact?, contactName: String?) {
        if (phone?.populated == true) {
            phoneNumber2.visibility = View.VISIBLE
            callCount2.visibility = View.VISIBLE
            lastDialed2.visibility = View.VISIBLE
            lastVoice2.visibility = View.VISIBLE

            contactName2.text = contactName
            phoneNumber2.text = phone?.number

            if (phone?.callCount != null) {
                callCount2.text = "${phone?.callCount} calls made"
            } else {
                callCount2.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastDialled != null) {
                lastDialed2.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastDialed2.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastVoice != null) {
                lastVoice2.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastVoice2.text = resources.getString(R.string.callmade)
            }
        } else {
            contactName2.text = resources.getString(R.string.missing_number)
            phoneNumber2.visibility = View.GONE
            callCount2.visibility = View.GONE
            lastDialed2.visibility = View.GONE
            lastVoice2.visibility = View.GONE
        }

        if (phone?.enabled == true) {
            disabled2.visibility = View.GONE
        } else {
            disabled2.visibility = View.VISIBLE
        }
    }

    private fun updatePhoneSlot3(phone: PhoneContact?, contactName: String?) {
        if (phone?.populated == true) {
            phoneNumber3.visibility = View.VISIBLE
            callCount3.visibility = View.VISIBLE
            lastDialed3.visibility = View.VISIBLE
            lastVoice3.visibility = View.VISIBLE

            contactName3.text = contactName
            phoneNumber3.text = phone?.number

            if (phone?.callCount != null) {
                callCount3.text = "${phone?.callCount} calls made"
            } else {
                callCount3.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastDialled != null) {
                lastDialed3.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastDialed3.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastVoice != null) {
                lastVoice3.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastVoice3.text = resources.getString(R.string.callmade)
            }
        } else {
            contactName3.text = resources.getString(R.string.missing_number)
            phoneNumber3.visibility = View.GONE
            callCount3.visibility = View.GONE
            lastDialed3.visibility = View.GONE
            lastVoice3.visibility = View.GONE
        }

        if (phone?.enabled == true) {
            disabled3.visibility = View.GONE
        } else {
            disabled3.visibility = View.VISIBLE
        }
    }

    private fun updatePhoneSlot4(phone: PhoneContact?, contactName: String?) {
        if (phone?.populated == true) {
            phoneNumber4.visibility = View.VISIBLE
            callCount4.visibility = View.VISIBLE
            lastDialed4.visibility = View.VISIBLE
            lastVoice4.visibility = View.VISIBLE

            contactName4.text = contactName
            phoneNumber4.text = phone?.number

            if (phone?.callCount != null) {
                callCount4.text = "${phone?.callCount} calls made"
            } else {
                callCount4.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastDialled != null) {
                lastDialed4.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastDialed4.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastVoice != null) {
                lastVoice4.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastVoice4.text = resources.getString(R.string.callmade)
            }
        } else {
            contactName4.text = resources.getString(R.string.missing_number)
            phoneNumber4.visibility = View.GONE
            callCount4.visibility = View.GONE
            lastDialed4.visibility = View.GONE
            lastVoice4.visibility = View.GONE
        }

        if (phone?.enabled == true) {
            disabled4.visibility = View.GONE
        } else {
            disabled4.visibility = View.VISIBLE
        }
    }

    private fun updatePhoneSlot5(phone: PhoneContact?, contactName: String?) {
        if (phone?.populated == true) {
            phoneNumber5.visibility = View.VISIBLE
            callCount5.visibility = View.VISIBLE
            lastDialed5.visibility = View.VISIBLE
            lastVoice5.visibility = View.VISIBLE

            contactName5.text = contactName
            phoneNumber5.text = phone?.number

            if (phone?.callCount != null) {
                callCount5.text = "${phone?.callCount} calls made"
            } else {
                callCount5.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastDialled != null) {
                lastDialed5.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastDialed5.text = resources.getString(R.string.callmade)
            }

            if (phone?.lastVoice != null) {
                lastVoice5.text = "Last Dialed ${phone?.lastDialled} "
            } else {
                lastVoice5.text = resources.getString(R.string.callmade)
            }
        } else {
            contactName5.text = resources.getString(R.string.missing_number)
            phoneNumber5.visibility = View.GONE
            callCount5.visibility = View.GONE
            lastDialed5.visibility = View.GONE
            lastVoice5.visibility = View.GONE
        }

        if (phone?.enabled == true) {
            disabled5.visibility = View.GONE
        } else {
            disabled5.visibility = View.VISIBLE
        }
    }

    private fun updateVolume() {
        with(BluetoothLeService.service) {
            volume.text = (this?.device?.microphoneLevel ?: "??").toString()
            microphone.text = (this?.device?.microphoneLevel ?: "??").toString()
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

                    updateVolume()
                }
                BluetoothLeService.ACTION_UPDATE_AUTHENTICATION -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_AUTHENTICATION.")
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_SLOT.")
                    updatePhoneSlots()
                }
                BluetoothLeService.ACTION_CLEAR_PHONE_SLOT -> {
                    Log.d(HomeActivity.TAG, "ACTION_CLEAR_PHONE_SLOT.")
                    updatePhoneSlots()
                }
                BluetoothLeService.ACTION_UPDATE_PHONE_CONFIG -> {
                    Log.d(HomeActivity.TAG, "ACTION_UPDATE_PHONE_CONFIG.")
                    updatePhoneConfig()
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