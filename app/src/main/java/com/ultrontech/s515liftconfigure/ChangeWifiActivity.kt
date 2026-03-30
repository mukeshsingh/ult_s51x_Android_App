package com.ultrontech.s515liftconfigure

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityChangeWifiBinding
import com.ultrontech.s515liftconfigure.util.EdgeToEdgeUtils
import com.ultrontech.s515liftconfigure.wheelpicker.LoopView

class ChangeWifiActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivityChangeWifiBinding
    private val bluetoothLeService: BluetoothLeService? = BluetoothLeService.service

    private lateinit var wifiManager: WifiManager
    private lateinit var loopViewWifi: LoopView
    private lateinit var loopViewSecurity: LoopView

    private var wifiList: MutableList<String> = mutableListOf()
    private var scanResults: List<ScanResult> = emptyList()
    private var selectedSsid: String = ""
    private var isManualEntry: Boolean = false

    // Screen states
    private enum class ScreenState {
        SCAN,           // Screen 1: Scan button
        CHOOSE_WIFI,    // Screen 2: WiFi picker with Connect/Enter Manually
        PASSWORD_ENTRY, // Screen 3: Password and security entry
        CONNECTED       // Screen 4: Connected state with Edit button
    }

    private var currentState: ScreenState = ScreenState.SCAN

    private val securityTypes = arrayListOf("No Security", "WPA", "WPA2", "WPA3")

    // BroadcastReceiver for WiFi status updates from the lift
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL -> {
                    Log.d(TAG, "ACTION_UPDATE_WIFI_DETAIL received from lift")
                    updateWifiDetail()
                    // Update connected state UI if we're on the connected screen
                    if (currentState == ScreenState.CONNECTED) {
                        val isActuallyConnected = bluetoothLeService?.device?.wifiConnected == true
                        if (isActuallyConnected) {
                            binding.txtConnectedWifiName.text = bluetoothLeService?.device?.connectedSSID ?: selectedSsid
                        }
                    }
                }
            }
        }
    }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) ?: false
            if (success) {
                scanSuccess()
            } else {
                scanFailure()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startWifiScan()
        } else {
            Toast.makeText(this, "Location permission is required to scan WiFi networks", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChangeWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        EdgeToEdgeUtils.handleRootWindowInsets(binding.root)
        window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        loopViewWifi = binding.loopViewWifi
        loopViewSecurity = binding.loopViewSecurity

        // Initialize security type picker
        loopViewSecurity.setArrayList(securityTypes)
        loopViewSecurity.selectedItem = 2 // Default to WPA2

        setupClickListeners()
        updateWifiDetail()

        // Hide loader initially
        hideLoader()

        // Check if WiFi is already connected and show appropriate screen
        if (bluetoothLeService?.device?.wifiConnected == true) {
            selectedSsid = bluetoothLeService.device?.connectedSSID ?: ""
            showState(ScreenState.CONNECTED)
        } else {
            showState(ScreenState.SCAN)
        }
    }

    private fun setupClickListeners() {
        // Screen 1: Scan WiFi button
        binding.btnScanWifi.setOnClickListener {
            checkPermissionsAndScan()
        }

        // Screen 2: Connect button (after selecting from picker)
        binding.btnConnectWifi.setOnClickListener {
            selectedSsid = wifiList.getOrNull(loopViewWifi.selectedItem) ?: ""
            isManualEntry = false
            if (selectedSsid.isNotEmpty()) {
                binding.edtWifiSsid.setText(selectedSsid)
                binding.edtWifiSsid.isEnabled = false
                showState(ScreenState.PASSWORD_ENTRY)
            }
        }

        // Screen 2: Enter Manually button
        binding.btnEnterManually.setOnClickListener {
            isManualEntry = true
            binding.edtWifiSsid.setText("")
            binding.edtWifiSsid.isEnabled = true
            showState(ScreenState.PASSWORD_ENTRY)
        }

        // Screen 3: Connect/Save button
        binding.btnConnectSave.setOnClickListener {
            val ssid = binding.edtWifiSsid.text.toString()
            val password = binding.edtWifiPassword.text.toString()
            val securityIndex = loopViewSecurity.selectedItem

            if (ssid.isEmpty()) {
                Toast.makeText(this, "Please enter WiFi name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save WiFi credentials via Bluetooth
            saveWifiCredentials(ssid, password, securityIndex)
        }

        // Screen 4: Edit button
        binding.btnEditWifi.setOnClickListener {
            showState(ScreenState.SCAN)
        }

        // Footer buttons
        binding.footer.btnHome.setOnClickListener {
            var intent = Intent(this, MyProductsActivity::class.java)
            if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
                intent = Intent(this, EngineerHomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.footer.btnBack.setOnClickListener {
            handleBackPress()
        }

        // ****************** Option Menu Start ******************
        binding.toolbar.optionBtn.setOnClickListener {
            if (binding.optionMenu.llOptionMenu.visibility == View.GONE) {
                binding.optionMenu.llOptionMenu.visibility = View.VISIBLE
            } else {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
            }
        }

        binding.optionMenu.llMenuAccount.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeWifiActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeWifiActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@ChangeWifiActivity, TroubleshootingActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@ChangeWifiActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }

    private fun handleBackPress() {
        when (currentState) {
            ScreenState.SCAN -> finish()
            ScreenState.CHOOSE_WIFI -> showState(ScreenState.SCAN)
            ScreenState.PASSWORD_ENTRY -> showState(ScreenState.CHOOSE_WIFI)
            ScreenState.CONNECTED -> finish()
        }
    }

    private fun showState(state: ScreenState) {
        currentState = state

        // Hide all state layouts
        binding.llScanWifi.visibility = View.GONE
        binding.llChooseWifi.visibility = View.GONE
        binding.llPasswordEntry.visibility = View.GONE
        binding.llConnectedState.visibility = View.GONE

        // Show the appropriate state layout
        when (state) {
            ScreenState.SCAN -> {
                binding.llScanWifi.visibility = View.VISIBLE
            }
            ScreenState.CHOOSE_WIFI -> {
                binding.llChooseWifi.visibility = View.VISIBLE
            }
            ScreenState.PASSWORD_ENTRY -> {
                binding.llPasswordEntry.visibility = View.VISIBLE
            }
            ScreenState.CONNECTED -> {
                binding.llConnectedState.visibility = View.VISIBLE
                binding.txtConnectedWifiName.text = selectedSsid
            }
        }

        updateWifiDetail()
    }

    private fun checkPermissionsAndScan() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            startWifiScan()
        }
    }

    private fun startWifiScan() {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(this, "Please enable WiFi", Toast.LENGTH_SHORT).show()
            return
        }

        showLoader()

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        val success = wifiManager.startScan()
        if (!success) {
            scanFailure()
        }
    }

    private fun scanSuccess() {
        hideLoader()

        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Receiver not registered: ${e.message}")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            scanResults = wifiManager.scanResults
            wifiList.clear()

            // Filter out empty SSIDs and duplicates
            val uniqueNetworks = scanResults
                .filter { it.SSID.isNotEmpty() }
                .distinctBy { it.SSID }
                .sortedByDescending { it.level }

            for (result in uniqueNetworks) {
                wifiList.add(result.SSID)
            }

            if (wifiList.isNotEmpty()) {
                loopViewWifi.setArrayList(ArrayList(wifiList))
                loopViewWifi.selectedItem = 0
                showState(ScreenState.CHOOSE_WIFI)
            } else {
                Toast.makeText(this, "No WiFi networks found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scanFailure() {
        hideLoader()

        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Receiver not registered: ${e.message}")
        }

        // Use cached results if available
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            scanResults = wifiManager.scanResults
            wifiList.clear()

            val uniqueNetworks = scanResults
                .filter { it.SSID.isNotEmpty() }
                .distinctBy { it.SSID }
                .sortedByDescending { it.level }

            for (result in uniqueNetworks) {
                wifiList.add(result.SSID)
            }

            if (wifiList.isNotEmpty()) {
                loopViewWifi.setArrayList(ArrayList(wifiList))
                loopViewWifi.selectedItem = 0
                showState(ScreenState.CHOOSE_WIFI)
            } else {
                Toast.makeText(this, "Could not scan WiFi networks", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveWifiCredentials(ssid: String, password: String, securityIndex: Int) {
        // Determine if password should be sent based on security type
        val passwordToSend = if (securityIndex == 0) "" else password // No security = empty password

        // Security type mapping: 1=No Security, 2=WPA, 3=WPA2, 4=WPA3
        val securityType = securityIndex + 1

        bluetoothLeService?.setSSID(ssid, passwordToSend, securityType)

        selectedSsid = ssid
        showState(ScreenState.CONNECTED)

        // Credentials sent to lift - actual connection status will be updated via ACTION_UPDATE_WIFI_DETAIL
        Toast.makeText(this, "WiFi credentials sent to lift", Toast.LENGTH_SHORT).show()
    }

    private fun updateWifiDetail() {
        // Only use actual wifiConnected status from lift, not local screen state
        val isConnected = bluetoothLeService?.device?.wifiConnected == true

        if (isConnected) {
            binding.wifiConnectedStatus.text = resources.getString(R.string.wifi_connected)
            binding.wifiConnectedStatus.setTextColor(resources.getColor(R.color.lightGreen, theme))
        } else {
            binding.wifiConnectedStatus.text = resources.getString(R.string.wifi_is_not_connected)
            binding.wifiConnectedStatus.setTextColor(resources.getColor(R.color.red, theme))
        }
    }

    override fun onResume() {
        super.onResume()
        // Register receiver for WiFi status updates from lift using LocalBroadcastManager
        val filter = IntentFilter(BluetoothLeService.ACTION_UPDATE_WIFI_DETAIL)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(bluetoothReceiver, filter)
        // Update WiFi status when resuming
        updateWifiDetail()
    }

    override fun onPause() {
        super.onPause()
        try {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(bluetoothReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth receiver not registered: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(wifiScanReceiver)
        } catch (e: Exception) {
            // Receiver was not registered
        }
    }

    private fun showLoader() {
        binding.loader.loaderView.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loader.loaderView.visibility = View.GONE
    }

    companion object {
        private const val TAG = "ChangeWifiActivity"
    }
}