package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.models.Util


class EditBoardDetailFragment : BottomSheetDialogFragment() {
    private lateinit var edtWifi: EditText
    private lateinit var edtJob: EditText
    private lateinit var edtClient: EditText
    private lateinit var edtPKey: EditText
    private lateinit var togglePinType: MaterialButtonToggleGroup
    private lateinit var btnNoPassPhrase: Button
    private lateinit var btnWithPassPhrase: Button
    private var security: Boolean = false
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
        edtPKey = view.findViewById(R.id.edtPKey)
        togglePinType = view.findViewById(R.id.btnPinType)
        btnNoPassPhrase = view.findViewById(R.id.btnNoPassPhase)
        btnWithPassPhrase = view.findViewById(R.id.btnWithPassPhase)

        edtWifi.setText(BluetoothLeService.service?.device?.connectedSSID)
        edtJob.setText(BluetoothLeService.service?.device?.job)
        edtClient.setText(BluetoothLeService.service?.device?.client)

        togglePinType.check(R.id.btnNoPassPhase)

        btnNoPassPhrase.setOnClickListener {
            security = false
            edtPKey.visibility = View.GONE
            edtPKey.setText("")
        }

        btnWithPassPhrase.setOnClickListener {
            security = true
            edtPKey.visibility = View.VISIBLE
        }

        btnUpdate.setOnClickListener {
            BluetoothLeService.service?.setSSID(edtWifi.text.toString(), if (security) edtPKey.text.toString() else "")
            Handler(Looper.getMainLooper()).postDelayed({
                BluetoothLeService.service?.setJob(
                    edtJob.text.toString(),
                    edtClient.text.toString()
                )
            },
                1000)
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditBoardDetailFragment).commit()
        }

        btnCancel.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditBoardDetailFragment).commit()
        }

        return view
    }
}