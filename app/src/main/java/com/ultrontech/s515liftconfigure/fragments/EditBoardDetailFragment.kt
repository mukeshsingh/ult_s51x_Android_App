package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService


class EditBoardDetailFragment : BottomSheetDialogFragment() {
    private lateinit var edtWifi: EditText
    private lateinit var edtJob: EditText
    private lateinit var edtClient: EditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_board_details, container, false)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        edtWifi = view.findViewById(R.id.edtSSID)
        edtJob = view.findViewById(R.id.edtJob)
        edtClient = view.findViewById(R.id.edtClient)

        edtWifi.setText(BluetoothLeService.service?.device?.ssid)
        edtJob.setText(BluetoothLeService.service?.device?.job)
        edtClient.setText(BluetoothLeService.service?.device?.client)

        btnUpdate.setOnClickListener {
            BluetoothLeService.service?.setJob(edtJob.text.toString(), edtClient.text.toString())
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditBoardDetailFragment).commit()
        }

        btnCancel.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditBoardDetailFragment).commit()
        }

        return view
    }
}