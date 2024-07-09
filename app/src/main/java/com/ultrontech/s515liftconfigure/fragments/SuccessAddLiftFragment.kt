package com.ultrontech.s515liftconfigure.fragments

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface.OnShowListener
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.AddLiftActivity
import com.ultrontech.s515liftconfigure.R

class SuccessAddLiftFragment : BottomSheetDialogFragment() {
    private lateinit var txtLiftName: TextView
    private lateinit var txtMsg: TextView
    private var liftName: String = ""
    private var msg: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.successful_transparent_screen, container, false)
        txtLiftName = view.findViewById(R.id.textSuccess2)
        txtMsg = view.findViewById(R.id.textSuccess3)

        txtLiftName.text = AddLiftActivity.lift?.name?: ""

        if (liftName.isNotEmpty()) txtLiftName.text = liftName
        if (msg.isNotEmpty()) txtMsg.text = msg

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener(OnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        })
        return dialog
    }

    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        val layoutParams = bottomSheet!!.layoutParams
        val windowHeight: Int = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireView().parent as View).setBackgroundColor(Color.TRANSPARENT)
    }

    fun updateMsg(liftName: String, msg: String) {
        this.liftName = liftName
        this.msg = msg
    }
}