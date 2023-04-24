package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.ultrontech.s515liftconfigure.adapters.LiftListAdapter
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothState
import com.ultrontech.s515liftconfigure.bluetooth.LiftBT
import com.ultrontech.s515liftconfigure.bluetooth.ScanDisplayItem
import com.ultrontech.s515liftconfigure.fragments.AddLiftFragment
import com.ultrontech.s515liftconfigure.models.PINNumber
import com.ultrontech.s515liftconfigure.models.UserLift

class FindLiftActivity : AppCompatActivity() {
    private var bluetoothService : BluetoothLeService? = null
    lateinit var addLiftFragment: AddLiftFragment
    private lateinit var liftList: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_lift)

        bluetoothService = BluetoothLeService.service

        liftList = findViewById<ListView>(R.id.lst_lifts)

        addLiftFragment = AddLiftFragment()

        liftList.adapter = bluetoothService?.lifts?.let { LiftListAdapter(this, it) }
    }

    fun showAddLiftDialog(lift: ScanDisplayItem) {
        addLiftFragment.lift = lift
        addLiftFragment.show(supportFragmentManager, "AddLiftFragment")
    }
    fun liftConnected() {
        liftList.adapter = bluetoothService?.lifts?.let { LiftListAdapter(this, it) }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_LIFT_LIST_UPDATED -> {
                    liftList.adapter = bluetoothService?.lifts?.let { LiftListAdapter(this@FindLiftActivity, it) }
                }
            }
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter? {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_LIFT_LIST_UPDATED)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }
}