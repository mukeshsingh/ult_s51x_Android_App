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
import com.ultrontech.s515liftconfigure.HomeActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.S515LiftConfigureApp


class LogoutFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(activity as HomeActivity, com.ultrontech.s515liftconfigure.R.style.TransparentBottomSheetDialogTheme)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_logout, container, false)
        val logoutBtn = view.findViewById<Button>(R.id.btnLogout)
        val cancelBtn = view.findViewById<Button>(R.id.btnCancel)

        logoutBtn.setOnClickListener {
            with(S515LiftConfigureApp) {
                profileStore.logout()
                (activity as HomeActivity).supportFragmentManager.beginTransaction().remove(this@LogoutFragment).commit()
                (activity as HomeActivity).loginChanged()
            }
        }

        cancelBtn.setOnClickListener {
            (activity as HomeActivity).supportFragmentManager.beginTransaction().remove(this@LogoutFragment).commit()
        }

        return view
    }
}