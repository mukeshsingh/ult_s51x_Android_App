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
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ultrontech.s515liftconfigure.adapters.RecyclerViewAdapter
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothState
import com.ultrontech.s515liftconfigure.databinding.ActivityEngineerHomeBinding
import com.ultrontech.s515liftconfigure.models.UserLift

class EngineerHomeActivity : AppCompatActivity() {
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEngineerHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(S515LiftConfigureApp) {
            noProduct = binding.noProduct
            lvUserLifts = binding.lvUserLifts

            var userDevices = profileStore.userDevices
            var data = userDevices.toList()
            if (userDevices.isNotEmpty()) {
                lvUserLifts.visibility = View.VISIBLE
                noProduct.visibility = View.GONE
            } else {
                lvUserLifts.visibility = View.GONE
                noProduct.visibility = View.VISIBLE
            }

            adapter = RecyclerViewAdapter(this@EngineerHomeActivity, data)
            lvUserLifts.adapter = adapter
            lvUserLifts.layoutManager = LinearLayoutManager(this@EngineerHomeActivity)

            // Setup swipe gestures using ItemTouchHelper
            itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Handle swipe
                    val position = viewHolder.layoutPosition
                    val item = adapter.getItem(position)

                    val vh = viewHolder as RecyclerViewAdapter.ViewHolder
                    if (direction == ItemTouchHelper.LEFT && vh.btnConnect.visibility == View.GONE) {
                        vh.btnConnect.visibility = View.VISIBLE
                        vh.btnRemove.visibility = View.GONE
                    } else if (direction == ItemTouchHelper.LEFT) {
                        vh.btnConnect.visibility = View.GONE
                        vh.btnRemove.visibility = View.GONE
                    } else if (direction == ItemTouchHelper.RIGHT && vh.btnRemove.visibility == View.GONE) {
                        vh.btnRemove.visibility = View.VISIBLE
                        vh.btnConnect.visibility = View.GONE
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        vh.btnRemove.visibility = View.GONE
                        vh.btnConnect.visibility = View.GONE
                    }

                    // Reset swipe action
                    itemTouchHelper.startSwipe(viewHolder)
                }
            })

            itemTouchHelper.attachToRecyclerView(lvUserLifts)
            btnFindLift = binding.footer
            btnFindLift.setOnClickListener {
                val intent = Intent(this@EngineerHomeActivity, SelectLiftTypeActivity::class.java)
                startActivity(intent)
            }

            binding.toolbar.optionBtn.background = null
            binding.toolbar.optionBtn.setOnClickListener {
                if (binding.optionMenu.llOptionMenu.visibility == View.GONE) {
                    binding.optionMenu.llOptionMenu.visibility = View.VISIBLE
                } else {
                    binding.optionMenu.llOptionMenu.visibility = View.GONE
                }
            }

            binding.optionMenu.llMenuAccount.setOnClickListener {
                binding.optionMenu.llOptionMenu.visibility = View.GONE

                val intent = Intent(this@EngineerHomeActivity, UserProfileActivity::class.java)
                startActivity(intent)
            }
            binding.optionMenu.llMenuLanguage.setOnClickListener {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
                val intent = Intent(this@EngineerHomeActivity, LanguageSelectorActivity::class.java)
                startActivity(intent)
            }
            binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
                val intent = Intent(this@EngineerHomeActivity, TroubleshootingActivity::class.java)
                startActivity(intent)

            }
            binding.optionMenu.llOptionMenu.setOnClickListener {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
            }
            binding.optionMenu.llLogout.setOnClickListener {
                binding.optionMenu.llOptionMenu.visibility = View.GONE

                with(S515LiftConfigureApp) {
                    profileStore.logout()
                    val intent = Intent(this@EngineerHomeActivity, SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

            binding.askLogout.llRemovePopup.setOnClickListener {
                binding.askLogout.llRemovePopup.visibility = View.GONE
            }
            binding.askLogout.btnYesRemove.setOnClickListener {
                binding.askLogout.llRemovePopup.visibility = View.GONE
                liftToRemove?.let { it1 ->
                    run {
                        profileStore.remove(it1)
                        liftToRemove = null
                        userDevices = profileStore.userDevices
                        data = userDevices.toList()
                        adapter = RecyclerViewAdapter(this@EngineerHomeActivity, data)
                        lvUserLifts.adapter = adapter
                    }
                }
            }
        }
    }

    private var liftToRemove: UserLift? = null
    private var liftToConnect: UserLift? = null
    fun showRemovePopup(lift: UserLift) {
        liftToRemove = lift
        binding.askLogout.llRemovePopup.visibility = View.VISIBLE
    }

    fun showConnectPopup(lift: UserLift) {
        liftToConnect = lift
//        binding.askLogout.llRemovePopup.visibility = View.VISIBLE
    }

    override fun onAttachedToWindow() {
        openOptionsMenu()
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

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTING -> {
                    showLoader()
                    updateConnectionState(BluetoothState.Connecting)
                }

                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    stopLoader()
                    updateConnectionState(BluetoothState.Connected)
                }

                BluetoothLeService.ACTION_GATT_CONNECTION_FAILURE -> {
                    stopLoader()
                    updateConnectionState(BluetoothState.ConnectionFailure)
                }

                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    stopLoader()
                    updateConnectionState(BluetoothState.NotConnected)
                }

                BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    bluetoothService?.updateServices()
                }

                BluetoothLeService.ACTION_GATT_SERVICES_AUTHENTICATED -> {
                    stopLoader()
                    // Show all the supported services and characteristics on the user interface.
                    updateConnectionState(BluetoothState.Connected)
                }

                BluetoothLeService.ACTION_BLUETOOTH_DEVICE_FOUND -> {
                    Log.d(HomeActivity.TAG, "Device found.")
                    stopLoader()
                    lvUserLifts.invalidate()
                    adapter.notifyDataSetChanged()
                }

                BluetoothLeService.ACTION_LIFT_LIST_UPDATED -> {
                    stopLoader()
                }
            }
        }
    }

    private fun stopLoader() {

    }

    private fun showLoader() {

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
        scanLifts()
        lvUserLifts.invalidate()
        adapter.notifyDataSetChanged()

        if (S515LiftConfigureApp.profileStore.userName.isNotEmpty()) {
            val name = S515LiftConfigureApp.profileStore.userName.replaceFirstChar { char -> char.uppercase()}
            binding.optionMenu.txtAccount.text = name
            binding.title.text = (name + " Lifts")
        } else {
            binding.optionMenu.txtAccount.text = resources.getString(R.string.account)
            binding.title.text = resources.getString(R.string.account)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        try { bluetoothService!!.close() } catch (e: Exception) { }
        try { bluetoothService!!.disconnect() } catch (e: Exception) { }
        try { BluetoothLeService.service!!.unbindService(serviceConnection) } catch (e: Exception) { }
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
            addAction(BluetoothLeService.ACTION_LIFT_LIST_UPDATED)
        }
    }

    companion object {
        const val TAG = "EngineerHomeActivity"
    }

    lateinit var binding: ActivityEngineerHomeBinding
    private lateinit var btnFindLift: Toolbar
    private lateinit var noProduct: LinearLayout
    private var state: BluetoothState = BluetoothState.Connecting
    private var bluetoothService : BluetoothLeService? = null
    private lateinit var lvUserLifts: RecyclerView
}