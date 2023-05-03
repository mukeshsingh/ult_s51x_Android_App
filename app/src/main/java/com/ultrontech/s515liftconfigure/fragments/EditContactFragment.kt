package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.setContact
import com.ultrontech.s515liftconfigure.models.PhoneContact


class EditContactFragment : BottomSheetDialogFragment() {
    var numberSlot = 1
    var phone: PhoneContact? = null
    var name: String? = null

    private lateinit var contactName: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var swtEnabled: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_contact, container, false)
        contactName = view.findViewById(R.id.edtContactName)
        phoneNumber = view.findViewById(R.id.edtPhoneNumber)
        swtEnabled = view.findViewById(R.id.swtEnabled)

        contactName.setText(name)
        phoneNumber.setText(phone?.number)
        swtEnabled.isEnabled = phone?.enabled == true

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)

        btnCancel.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditContactFragment).commit()
        }

        btnUpdate.setOnClickListener {
            with(BluetoothLeService.service) {
                this?.setContact(numberSlot, contactName.text.toString())
                this?.setPhoneNumber(
                    numberSlot,
                    swtEnabled.isEnabled,
                    phoneNumber.text.toString()
                )
            }

            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction()
                .remove(this@EditContactFragment).commit()
        }

        return view
    }


}