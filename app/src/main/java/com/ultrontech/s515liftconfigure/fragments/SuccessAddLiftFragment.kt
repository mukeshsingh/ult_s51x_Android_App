package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.AddLiftActivity
import com.ultrontech.s515liftconfigure.R

class SuccessAddLiftFragment : BottomSheetDialogFragment() {
    private lateinit var addLiftActivity: AddLiftActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        addLiftActivity = (activity as AddLiftActivity)

        return inflater.inflate(R.layout.successful_transparent_screen, container, false)
    }
}