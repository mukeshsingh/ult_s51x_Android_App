

package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.models.SimType
import com.ultrontech.s515liftconfigure.models.Util
import com.ultrontech.s515liftconfigure.wheelpicker.LoopView


class EditSimFragment : BottomSheetDialogFragment() {
    lateinit var loopView: LoopView
    private lateinit var btnPinType: MaterialButtonToggleGroup
    lateinit var btnPinLength: MaterialButtonToggleGroup
    lateinit var btn1: Button
    lateinit var btn2: Button
    lateinit var btn3: Button
    lateinit var btn4: Button
    lateinit var btn5: Button
    lateinit var btn6: Button
    lateinit var btn7: Button
    lateinit var btn8: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_sim, container, false)
        loopView = view.findViewById<LoopView>(R.id.loopView)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnUpdate = view.findViewById<Button>(R.id.btnUpdate)
        btnPinType = view.findViewById(R.id.btnPinType)
        btnPinLength = view.findViewById(R.id.btnPinLength)
        btnPinType.check(R.id.btnNoPin)
        btnPinLength.check(R.id.btn6)

        loopView.setArrayList(arrayListOf(Util.getSimTypeName(SimType.ModemSimTypeInstallerProvided), Util.getSimTypeName(SimType.ModemSimTypeUserPAYG), Util.getSimTypeName(SimType.ModemSimTypeUserContract), Util.getSimTypeName(SimType.ModemSimTypeUnknown)))
        btnCancel.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditSimFragment).commit()
        }

        return view
    }


}