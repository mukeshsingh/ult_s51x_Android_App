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
import com.ultrontech.s515liftconfigure.bluetooth.setName

class ChangeNameFragment : BottomSheetDialogFragment() {
    private lateinit var edtName: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_name, container, false)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        edtName = view.findViewById(R.id.edtName)

        btnUpdate.setOnClickListener {
            val name = edtName.text.toString()

            BluetoothLeService.service?.setName(name)
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@ChangeNameFragment).commit()
        }
        btnCancel.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@ChangeNameFragment).commit()
        }

        return view
    }
}