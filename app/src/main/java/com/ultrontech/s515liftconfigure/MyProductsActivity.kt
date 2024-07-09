package com.ultrontech.s515liftconfigure

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothState
import com.ultrontech.s515liftconfigure.databinding.ActivityMyProductsBinding
import com.ultrontech.s515liftconfigure.models.Device

class MyProductsActivity : LangSupportBaseActivity() {
    private lateinit var binding: ActivityMyProductsBinding
    private lateinit var btnFindLift: Toolbar
    private lateinit var noProduct: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.swipeToRefresh.setOnRefreshListener {
            binding.swipeToRefresh.isRefreshing = false;
            scanLifts()
        }
        binding.swipeToRefresh.setColorSchemeResources(R.color.lightGreen,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark);

        btnFindLift = binding.footer
        noProduct = binding.noProduct
        llUserLifts = binding.llUserLifts

        btnFindLift.setOnClickListener {
            val intent = Intent(this, FindLiftActivity::class.java)
            startActivity(intent)
        }

        inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // ****************** Option Menu Start ******************
        binding.toolbar.optionBtn.setOnClickListener {
            if (binding.optionMenu.llOptionMenu.visibility == View.GONE) {
                binding.optionMenu.llOptionMenu.visibility = View.VISIBLE
            } else {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
            }
        }

        val str = resources.getString(R.string.product_not_found)
        binding.txtNoProductFound.text = HtmlCompat.fromHtml(str, 0)

        binding.optionMenu.llMenuAccount.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@MyProductsActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        binding.optionMenu.version.visibility = View.VISIBLE
        binding.optionMenu.version.text = "Version ${BuildConfig.VERSION_NAME}"
        // ****************** Option Menu End ******************

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        scanLifts()
    }

    fun preventClicks(view: View?) {}

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            Log.e(HomeActivity.TAG, ">>>>>>>> BluetoothLeService serviceConnection.")
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            Log.e(HomeActivity.TAG, ">>>>>>>> BluetoothLeService serviceConnection.")
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")

                    S515LiftConfigureApp.instance.basicAlert(
                        this@MyProductsActivity, "Bluetooth service is mot available."
                    ) { finish() }
                } else {
                    Log.e(HomeActivity.TAG, ">>>>>>>> Device connected initialized.")

                    bluetooth.scanLeDevice()
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    private fun scanLifts() {
        val isSDKSandAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        when {
            (!isSDKSandAbove && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED) || (isSDKSandAbove && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN,
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED) -> {
                bluetoothService = BluetoothLeService.service
                if (bluetoothService != null) {
                    bluetoothService?.scanLeDevice()
                } else {
                    val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
                    bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
            }

            (!isSDKSandAbove && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) ||
                    (isSDKSandAbove && shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_CONNECT)) ||
                    (isSDKSandAbove && shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) -> {
                showInContextUI()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    ))
                } else {
                    requestPermissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ))
                }
            }
        }
    }

    private fun showInContextUI() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.permission_reason_dialog_title)
        builder.setMessage(R.string.permission_reason_msg)
        builder.setIcon(android.R.drawable.ic_dialog_info)
        builder.setPositiveButton("Ok"){dialogInterface, which ->
            Log.d(HomeActivity.TAG, "User seen message");
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            if (results[Manifest.permission.BLUETOOTH_SCAN] == true && results[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                bluetoothService = BluetoothLeService.service
                if (bluetoothService != null) {
                    bluetoothService?.scanLeDevice()
                } else {
                    val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
                    bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
            } else {
                showInContextUI()
            }
        }

    private fun showUserDevices() {
        llUserLifts.removeAllViews()
        val userDevices = S515LiftConfigureApp.profileStore.userDevices
        if (userDevices.isNotEmpty()) {
            llUserLifts.visibility = View.VISIBLE
            noProduct.visibility = View.GONE
        } else {
            llUserLifts.visibility = View.GONE
            noProduct.visibility = View.VISIBLE
        }

        userDevices.forEach {userLift ->
            val cardView = inflater.inflate(R.layout.card_component, null, false)
            val liftName = cardView.findViewById<TextView>(R.id.txt_lift_name)
            val btnEditLiftDetail = cardView.findViewById<Button>(R.id.btnEditLiftDetail)
            val onlineIcon = cardView.findViewById<ImageView>(R.id.img_online_icon)
            val offlineIcon = cardView.findViewById<ImageView>(R.id.img_offline_icon)
            val status = cardView.findViewById<TextView>(R.id.txt_status)
            val device = BluetoothLeService.service?.find(userLift.liftId)
            if (device != null) {
                offlineIcon.visibility = View.GONE
                onlineIcon.visibility = View.VISIBLE
                status.text = resources.getText(R.string.status_available)
            } else {
                offlineIcon.visibility = View.VISIBLE
                onlineIcon.visibility = View.GONE
                status.text = resources.getText(R.string.status_not_available)
            }
            liftName.text = userLift.liftName

            btnEditLiftDetail.setOnClickListener {
                if (device != null) {
                    val intent = Intent(this, UserLiftSettingsActivity::class.java)
                    intent.putExtra(HomeActivity.INTENT_LIFT_ID, userLift.liftId)
                    startActivity(intent)

                    BluetoothLeService.service?.updateCount = 0
                    linkDevice(userLift.liftId)
                    BluetoothLeService.service?.connect(userLift.liftId, userLift.liftName)
                }
            }

            llUserLifts.addView(cardView)
        }
    }

    private fun linkDevice (liftId: String) {
        val lift = S515LiftConfigureApp.profileStore.find(liftId)
        if (lift != null) {
            val device = Device(lift = lift)
            BluetoothLeService.service?.link(device)
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTING -> {
                    showLoader()
                    updateConnectionState(BluetoothState.Connecting)
                }

                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    hideLoader()
                    updateConnectionState(BluetoothState.Connected)
                }

                BluetoothLeService.ACTION_GATT_CONNECTION_FAILURE -> {
                    hideLoader()
                    updateConnectionState(BluetoothState.ConnectionFailure)
                }

                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    hideLoader()
                    updateConnectionState(BluetoothState.NotConnected)
                }

                BluetoothLeService.ACTION_BLUETOOTH_DEVICE_SCANNING -> {
                    showLoader()
                }

                BluetoothLeService.ACTION_BLUETOOTH_DEVICE_SCANNING_STOPPED -> {
                    hideLoader()
                    showUserDevices()
                }
                BluetoothLeService.ACTION_GATT_SERVICES_AUTHENTICATED -> {
                    // Show all the supported services and characteristics on the user interface.
                    updateConnectionState(BluetoothState.Connected)
                }

                BluetoothLeService.ACTION_BLUETOOTH_DEVICE_FOUND -> {
                    Log.d(HomeActivity.TAG, "Device found.")
                    showUserDevices()
                }
            }
        }
    }

    private fun hideLoader() {
        binding.loader.loaderView.visibility = View.GONE
    }

    private fun showLoader() {
        binding.loader.loaderView.visibility = View.VISIBLE
    }

    fun updateConnectionState(state: BluetoothState) {
        this.state = state
        when (state) {
            BluetoothState.Connected -> {
                Log.d(HomeActivity.TAG, ">>>>>>>>>>>>>>>>> Bluetooth connected")
            }
            BluetoothState.Connecting -> {
                Log.d(HomeActivity.TAG, ">>>>>>>>>>>>>>>>> Bluetooth connecting")
            }
            BluetoothState.Authenticated -> {
                Log.d(HomeActivity.TAG, ">>>>>>>>>>>>>>>>> Bluetooth authenticated")
            }
            BluetoothState.NotConnected -> {
                Log.d(HomeActivity.TAG, ">>>>>>>>>>>>>>>>> Bluetooth not connected")
            }
            BluetoothState.ConnectionFailure -> {
                Log.d(HomeActivity.TAG, ">>>>>>>>>>>>>>>>> Bluetooth connection failure")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showUserDevices()

        if (S515LiftConfigureApp.profileStore.userName.isNotEmpty()) {
            val name = S515LiftConfigureApp.profileStore.userName.replaceFirstChar { char -> char.uppercase()}
            binding.optionMenu.txtAccount.text = name
        } else {
            binding.optionMenu.txtAccount.text = resources.getString(R.string.account)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(gattUpdateReceiver)

        try { bluetoothService!!.close() } catch (_: Exception) { }
        try { bluetoothService!!.disconnect() } catch (_: Exception) { }
        try { BluetoothLeService.service!!.unbindService(serviceConnection) } catch (_: Exception) { }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTING)
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_CONNECTION_FAILURE)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_AUTHENTICATED)
            addAction(BluetoothLeService.ACTION_BLUETOOTH_DEVICE_FOUND)
            addAction(BluetoothLeService.ACTION_LIFT_LIST_UPDATED)
            addAction(BluetoothLeService.ACTION_BLUETOOTH_DEVICE_SCANNING)
            addAction(BluetoothLeService.ACTION_BLUETOOTH_DEVICE_SCANNING_STOPPED)
        }
    }

    companion object {
        const val TAG = "MyProductActivity"
        const val INTENT_LIFT_ID = "com.ultrontech.s515liftconfigure.INTENT_LIFT_ID"
    }

    private var state: BluetoothState = BluetoothState.Connecting
    private var bluetoothService : BluetoothLeService? = null
    private lateinit var llUserLifts: LinearLayout
    private lateinit var inflater: LayoutInflater
    private lateinit var userName: TextView}