package com.ultrontech.s515liftconfigure.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.HomeActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.S515LiftConfigureApp
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService


class EditLiftFragment : BottomSheetDialogFragment() {
    private val editBoardDetailsFragment: EditBoardDetailsFragment = EditBoardDetailsFragment()
    private val editChangeNameFragment: ChangeNameFragment = ChangeNameFragment()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(activity as EngineerDetailsActivity, com.ultrontech.s515liftconfigure.R.style.TransparentBottomSheetDialogTheme)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_lift, container, false)
        val disconnectBtn = view.findViewById<Button>(R.id.btn_disconnect)
        val cancelBtn = view.findViewById<Button>(R.id.btnCancel)
        val changeNameBtn = view.findViewById<Button>(R.id.btn_change_name)
        val changePinBtn = view.findViewById<Button>(R.id.btn_change_pin)

        changeNameBtn.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditLiftFragment).commit()
            editChangeNameFragment.show((activity as EngineerDetailsActivity).supportFragmentManager, "editChangeNameFragment")
        }
        changePinBtn.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditLiftFragment).commit()
            editBoardDetailsFragment.show((activity as EngineerDetailsActivity).supportFragmentManager, "editBoardDetailsFragment")
        }
        disconnectBtn.setOnClickListener {
            activity?.finish()
        }

        cancelBtn.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditLiftFragment).commit()
        }

        return view
    }
}