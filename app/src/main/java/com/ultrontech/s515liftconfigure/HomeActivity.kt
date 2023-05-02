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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothState
import com.ultrontech.s515liftconfigure.bluetooth.LiftBT
import com.ultrontech.s515liftconfigure.fragments.EditSimFragment
import com.ultrontech.s515liftconfigure.fragments.LoginPinFragment
import com.ultrontech.s515liftconfigure.fragments.LogoutFragment
import com.ultrontech.s515liftconfigure.models.LiftDevice
import com.ultrontech.s515liftconfigure.models.ProfileStore
import com.ultrontech.s515liftconfigure.models.UserLift
import org.w3c.dom.Text

class HomeActivity : AppCompatActivity() {
    private lateinit var llUserLifts: LinearLayout
    private lateinit var inflater: LayoutInflater
    private lateinit var userName: TextView

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            Log.e(TAG, ">>>>>>>> BluetoothLeService serviceConnection.")
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            Log.e(TAG, ">>>>>>>> BluetoothLeService serviceConnection.")
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                    finish()
                }

                Log.e(TAG, ">>>>>>>> Device connected initialized.")

                bluetooth.scanLeDevice()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val logoutFragmentSheet = LogoutFragment()
        val editSimFragment = EditSimFragment()
        val loginPinFragment = LoginPinFragment()

        var isLoggedIn = false

        llUserLifts = findViewById<LinearLayout>(R.id.ll_home_lifts)
        val findLift = findViewById<CardView>(R.id.cvFindLift)
        val userProfile = findViewById<CardView>(R.id.cvProfile)
        val enterLoginPin = findViewById<CardView>(R.id.cvLogin)

        userName = findViewById(R.id.txt_user_name)
        if (S515LiftConfigureApp.profileStore.userName.isNotEmpty()) {
            userName.visibility = View.VISIBLE
            userName.text = S515LiftConfigureApp.profileStore.userName
        }else{
            userName.visibility = View.GONE
        }


        inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        findLift.setOnClickListener {
            val intent = Intent(this, FindLiftActivity::class.java)
            startActivity(intent)
        }

        userProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        enterLoginPin.setOnClickListener {
            with(S515LiftConfigureApp) {
                if (profileStore.hasEngineerCapability) {
                    logoutFragmentSheet.show(supportFragmentManager, "LogoutFragment")
                } else {
                    loginPinFragment.show(supportFragmentManager, "LoginPinFragment")
                }
            }
        }

        loginChanged()

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())

        scanLifts()
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
                val intent = Intent(this, EngineerDetailsActivity::class.java)
                intent.putExtra(INTENT_LIFT_ID, userLift.liftId)
                startActivity(intent)
            }

            llUserLifts.addView(cardView)
        }
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
            Log.d(TAG, "User seen message");
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
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
                    Log.d(TAG, "Device found.")
                }
            }
        }
    }

    fun updateConnectionState(state: BluetoothState) {
        this.state = state
        when (state) {
            BluetoothState.Connected -> {
                Log.d(TAG, ">>>>>>>>>>>>>>>>> Bluetooth connected")
            }
            BluetoothState.Connecting -> {
                Log.d(TAG, ">>>>>>>>>>>>>>>>> Bluetooth connecting")
            }
            BluetoothState.Authenticated -> {
                Log.d(TAG, ">>>>>>>>>>>>>>>>> Bluetooth authenticated")
            }
            BluetoothState.NotConnected -> {
                Log.d(TAG, ">>>>>>>>>>>>>>>>> Bluetooth not connected")
            }
            BluetoothState.ConnectionFailure -> {
                Log.d(TAG, ">>>>>>>>>>>>>>>>> Bluetooth connection failure")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (S515LiftConfigureApp.profileStore.userName.isNotEmpty()) {
            userName.visibility = View.VISIBLE
            userName.text = S515LiftConfigureApp.profileStore.userName
        }else{
            userName.visibility = View.GONE
        }
        showUserDevices()
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

    fun loginChanged() {
        with(S515LiftConfigureApp) {
            val title = findViewById<TextView>(R.id.txtEngineerLoginTitle)
            val desc = findViewById<TextView>(R.id.txtEngineerLoginDesc)

            if (profileStore.hasEngineerCapability) {
                title.setText(R.string.engineer_logged_in_title)
                desc.setText(R.string.engineer_logged_in_desc)
            } else {
                title.setText(R.string.engineer_logged_out_title)
                desc.setText(R.string.engineer_logged_out_desc)
            }
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
        const val TAG = "HomeActivity"
        const val INTENT_LIFT_ID = "com.ultrontech.s515liftconfigure.INTENT_LIFT_ID"
    }

    private var state: BluetoothState = BluetoothState.Connecting
    lateinit var device: LiftDevice
    private var bluetoothService : BluetoothLeService? = null
    private lateinit var rlConnecting: RelativeLayout
    private lateinit var rlTabView: RelativeLayout
    private lateinit var rlError: RelativeLayout

    private lateinit var txtConnectingTitle: TextView
}
