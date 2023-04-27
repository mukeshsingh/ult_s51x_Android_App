package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.R


class EditCallDelayFragment : BottomSheetDialogFragment() {
    val BAR_VALUE = 5
    private var value = 0
    private lateinit var bars: Array<View>

    fun setValue(value: Int) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_call_delay, container, false)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        val vBar1 = view.findViewById<View>(R.id.vBar1)
        val vBar2 = view.findViewById<View>(R.id.vBar2)
        val vBar3 = view.findViewById<View>(R.id.vBar3)
        val vBar4 = view.findViewById<View>(R.id.vBar4)
        val vBar5 = view.findViewById<View>(R.id.vBar5)
        val vBar6 = view.findViewById<View>(R.id.vBar6)
        val vBar7 = view.findViewById<View>(R.id.vBar7)
        val vBar8 = view.findViewById<View>(R.id.vBar8)
        val vBar9 = view.findViewById<View>(R.id.vBar9)
        val vBar10 = view.findViewById<View>(R.id.vBar10)

        bars = arrayOf(vBar1, vBar2, vBar3, vBar4, vBar5, vBar6, vBar7, vBar8, vBar9, vBar10)
        bars.forEachIndexed { index, bar ->
            bar.setOnClickListener {
                setValue(BAR_VALUE * (index + 1))
            }
        }

        btnClose.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditCallDelayFragment).commit()
        }

        return view
    }
}