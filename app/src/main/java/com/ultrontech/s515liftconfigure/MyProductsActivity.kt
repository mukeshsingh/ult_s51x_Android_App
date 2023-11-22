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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothState
import com.ultrontech.s515liftconfigure.databinding.ActivityMyProductsBinding

class MyProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProductsBinding
    private lateinit var btnFindLift: Toolbar
    private lateinit var noProduct: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyProductsBinding.inflate(layoutInflater)
        btnFindLift = binding.btnFindLift
        noProduct = binding.noProduct

        btnFindLift.setOnClickListener {
            val intent = Intent(this, FindLiftActivity::class.java)
            startActivity(intent)
        }

        setContentView(binding.root)

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())

        scanLifts()
    }

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
                    Log.e(HomeActivity.TAG, "Unable to initialize Bluetooth")
                    finish()
                }

                Log.e(HomeActivity.TAG, ">>>>>>>> Device connected initialized.")

                bluetooth.scanLeDevice()
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

        S515LiftConfigureApp.profileStore.userDevices.forEach {userLift ->
            val cardView = inflater.inflate(R.layout.home_lift_list_item, null, false)
            val liftName = cardView.findViewById<TextView>(R.id.txt_lift_name)
            val liftMsg = cardView.findViewById<TextView>(R.id.txt_lift_msg)
            liftName.text = userLift.liftName
            liftMsg.text = resources.getString(R.string.lift_status)

            cardView.setOnClickListener {
                val lift = BluetoothLeService.service?.find(userLift.liftId)
                if (lift?.modelNumber != null && lift.modelNumber!!.isNotEmpty()) {
                    val intent = Intent(this, EngineerDetailsActivity::class.java)
                    intent.putExtra(HomeActivity.INTENT_LIFT_ID, userLift.liftId)
                    startActivity(intent)
                } else {
                    this@MyProductsActivity?.let { it1 ->
                        S515LiftConfigureApp.instance.basicAlert(
                            it1, "The mobile application is waiting for the connected BT device to finish processing."
                        ){}
                    }
                }
            }

            llUserLifts.addView(cardView)
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTING -> {
                    updateConnectionState(BluetoothState.Connecting)
                }

                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    updateConnectionState(BluetoothState.Connected)
                }

                BluetoothLeService.ACTION_GATT_CONNECTION_FAILURE -> {
                    updateConnectionState(BluetoothState.ConnectionFailure)
                }

                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    updateConnectionState(BluetoothState.NotConnected)
                }

                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    bluetoothService?.updateServices()
                }

                BluetoothLeService.ACTION_GATT_SERVICES_AUTHENTICATED -> {
                    // Show all the supported services and characteristics on the user interface.
                    updateConnectionState(BluetoothState.Connected)
                }

                BluetoothLeService.ACTION_BLUETOOTH_DEVICE_FOUND -> {
                    Log.d(HomeActivity.TAG, "Device found.")
                }
            }
        }
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
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
//        showUserDevices()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (bluetoothService != null) {
            bluetoothService!!.close()
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTING)
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_CONNECTION_FAILURE)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(BluetoothLeService.ACTION_GATT_SERVICES_AUTHENTICATED)
            addAction(BluetoothLeService.ACTION_BLUETOOTH_DEVICE_FOUND)
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