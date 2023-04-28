package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.FindLiftActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.S515LiftConfigureApp
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.bluetooth.ScanDisplayItem
import com.ultrontech.s515liftconfigure.bluetooth.setAccess
import com.ultrontech.s515liftconfigure.listener.PinOnKeyListener
import com.ultrontech.s515liftconfigure.models.PINNumber
import com.ultrontech.s515liftconfigure.models.UserLift
import com.ultrontech.s515liftconfigure.watcher.PinTextWatcher

class ChangePinFragment : BottomSheetDialogFragment() {
    private lateinit var engineerDetailsActivity: EngineerDetailsActivity
    private lateinit var p1: EditText
    private lateinit var p2: EditText
    private lateinit var p3: EditText
    private lateinit var p4: EditText
    private lateinit var p5: EditText
    private lateinit var p6: EditText
    private lateinit var editTexts: Array<EditText>
    var lift: ScanDisplayItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        engineerDetailsActivity = (activity as EngineerDetailsActivity)

        val view = inflater.inflate(R.layout.fragment_edit_pin, container, false)
        val connectButton = view.findViewById<Button>(R.id.btnConnect)
        val cancelButton = view.findViewById<Button>(R.id.btnCancel)
        val currentPin = view.findViewById<TextView>(R.id.txt_current_pinNumber)
        val lift = BluetoothLeService.service?.device?.lift?.liftId?.let {
            S515LiftConfigureApp.profileStore.find(
                it
            )
        }

        currentPin.text = lift?.accessKey?.display()

        p1 = view.findViewById(R.id.edtPin1)
        p2 = view.findViewById(R.id.edtPin2)
        p3 = view.findViewById(R.id.edtPin3)
        p4 = view.findViewById(R.id.edtPin4)
        p5 = view.findViewById(R.id.edtPin5)
        p6 = view.findViewById(R.id.edtPin6)

        editTexts = arrayOf(p1, p2, p3, p4, p5, p6)

        connectButton.setOnClickListener {
            with(S515LiftConfigureApp) {
                val pinStr = "${p1.text}${p2.text}${p3.text}${p4.text}${p5.text}${p6.text}".trim()

                if (pinStr.length == 6) {
                    val accessKey = pinStr.map { it.digitToInt() }.toIntArray()

                    BluetoothLeService.service?.setAccess(PINNumber(6, accessKey))
                    editTexts.forEach {
                        it.setText("")
                    }
                    (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@ChangePinFragment).commit()
                }
            }
        }

        p1.addTextChangedListener(PinTextWatcher(0, editTexts, activity as EngineerDetailsActivity));
        p2.addTextChangedListener(PinTextWatcher(1, editTexts, activity as EngineerDetailsActivity));
        p3.addTextChangedListener(PinTextWatcher(2, editTexts, activity as EngineerDetailsActivity));
        p4.addTextChangedListener(PinTextWatcher(3, editTexts, activity as EngineerDetailsActivity));
        p5.addTextChangedListener(PinTextWatcher(4, editTexts, activity as EngineerDetailsActivity));
        p6.addTextChangedListener(PinTextWatcher(5, editTexts, activity as EngineerDetailsActivity));

        p1.setOnKeyListener(PinOnKeyListener(0, editTexts))
        p2.setOnKeyListener(PinOnKeyListener(1, editTexts))
        p3.setOnKeyListener(PinOnKeyListener(2, editTexts))
        p4.setOnKeyListener(PinOnKeyListener(3, editTexts))
        p5.setOnKeyListener(PinOnKeyListener(4, editTexts))
        p6.setOnKeyListener(PinOnKeyListener(5, editTexts))

        cancelButton.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@ChangePinFragment).commit()
        }
        return view
    }
}