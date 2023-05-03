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


class EditMicrophoneFragment : BottomSheetDialogFragment() {
    private val barValue = 1
    private var value = 1
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
        val view = inflater.inflate(R.layout.fragment_edit_volume, container, false)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        txtValue = view.findViewById(R.id.txt_value)
        val title = view.findViewById<TextView>(R.id.txt_title)
        title.text = resources.getString(R.string.micro)

        val vBar1 = view.findViewById<View>(R.id.vBar1)
        val vBar2 = view.findViewById<View>(R.id.vBar2)
        val vBar3 = view.findViewById<View>(R.id.vBar3)
        val vBar4 = view.findViewById<View>(R.id.vBar4)
        val vBar5 = view.findViewById<View>(R.id.vBar5)

        bars = arrayOf(vBar1, vBar2, vBar3, vBar4, vBar5)
        bars.forEachIndexed { index, bar ->
            bar.setOnClickListener {
                setValue(barValue * (index + 1))
            }
        }

        btnClose.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditMicrophoneFragment).commit()
        }

        with(BluetoothLeService.service?.device) {
            setValue(this?.microphoneLevel ?: 1)
            txtValue.text = (this?.microphoneLevel ?: 1).toString()
        }

        return view
    }
}