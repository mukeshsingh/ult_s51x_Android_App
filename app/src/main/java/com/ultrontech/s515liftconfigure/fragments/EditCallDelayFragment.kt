package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService


class EditCallDelayFragment : BottomSheetDialogFragment() {
    private val barValue = 5
    private var value = 0
    private lateinit var bars: Array<View>
    private lateinit var txtValue: TextView

    fun setValue(value: Int) {
        val idx = (value / barValue) - 1

        txtValue.text = value.toString()
        bars.forEachIndexed { index, bar ->
            if (index <= idx) {
                bar.setBackgroundColor(resources.getColor(R.color.green, (activity as EngineerDetailsActivity).theme))
            } else {
                bar.setBackgroundColor(resources.getColor(R.color.grey, (activity as EngineerDetailsActivity).theme))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_call_delay, container, false)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        txtValue = view.findViewById(R.id.txt_value)

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
                setValue(barValue * (index + 1))
            }
        }

        btnClose.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditCallDelayFragment).commit()
        }

        BluetoothLeService.service?.device?.callPressDelay?.let {
            setValue(it)
            txtValue.text = it.toString()
        }

        return view
    }
}