package com.ultrontech.s515liftconfigure

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.ultrontech.s515liftconfigure.adapters.LiftListAdapter
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.fragments.AddLiftFragment

class FindLiftActivity : AppCompatActivity() {
    private var bluetoothService : BluetoothLeService? = null
    lateinit var addLiftFragment: AddLiftFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_lift)

        bluetoothService = BluetoothLeService.service

        val liftList = findViewById<ListView>(R.id.lst_lifts)

        addLiftFragment = AddLiftFragment()

        liftList.adapter = bluetoothService?.lifts?.let { LiftListAdapter(this, it) }
    }

    fun showAddLiftDialog() {
        addLiftFragment.show(supportFragmentManager, "AddLiftFragment")
    }
    fun connectLift() {

    }
}