package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.HomeActivity
import com.ultrontech.s515liftconfigure.R


class LoginPinFragment : BottomSheetDialogFragment() {
    lateinit var homeActivity: HomeActivity
    // TODO: Rename and change types of parameters
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        homeActivity = (activity as HomeActivity)

        val view = inflater.inflate(R.layout.fragment_login_pin, container, false)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val cancelButton = view.findViewById<Button>(R.id.btnCancel)

        return view
    }
}