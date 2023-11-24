package com.ultrontech.s515liftconfigure

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.ultrontech.s515liftconfigure.adapters.LiftListAdapter
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.ScanDisplayItem

class FindLiftActivity : AppCompatActivity() {
    private var bluetoothService : BluetoothLeService? = null
    private lateinit var liftList: ListView
    private lateinit var homeBtn: ImageButton
    private lateinit var backBtn: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_lift)

        bluetoothService = BluetoothLeService.service

        liftList = findViewById(R.id.lst_lifts)
        homeBtn = findViewById(R.id.btn_home)
        backBtn = findViewById(R.id.btn_back)

        liftList.adapter = bluetoothService?.lifts?.let { LiftListAdapter(this, it) }

        homeBtn.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
            startActivity(intent)
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    fun goToAddLift(lift: ScanDisplayItem) {
        AddLiftActivity.lift = lift
        val intent = Intent(this, AddLiftActivity::class.java)
        startActivity(intent)
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