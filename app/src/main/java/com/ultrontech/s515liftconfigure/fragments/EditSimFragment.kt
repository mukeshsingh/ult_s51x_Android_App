

package com.ultrontech.s515liftconfigure.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.ultrontech.s515liftconfigure.EngineerDetailsActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.listener.PinOnKeyListener
import com.ultrontech.s515liftconfigure.models.PINNumber
import com.ultrontech.s515liftconfigure.models.SimType
import com.ultrontech.s515liftconfigure.models.Util
import com.ultrontech.s515liftconfigure.watcher.PinTextWatcher
import com.ultrontech.s515liftconfigure.wheelpicker.LoopView


class EditSimFragment : BottomSheetDialogFragment() {
    private lateinit var loopView: LoopView
    private lateinit var btnPinType: MaterialButtonToggleGroup
    private lateinit var btnPinLength: MaterialButtonToggleGroup
    private lateinit var llPinRequired: LinearLayout
    private lateinit var btnNoPin: Button
    private lateinit var btnPinRequired: Button

    private lateinit var buttons: Array<Button>
    private lateinit var edtPins: Array<EditText>
    private var isPinRequired: Boolean = false
    private var pinLength: Int = 6
    private lateinit var textWatchers: Array<PinTextWatcher>
    private lateinit var onKeyListeners: Array<PinOnKeyListener>

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
        llPinRequired = view.findViewById(R.id.ll_pin_required)
        btnNoPin = view.findViewById(R.id.btnNoPin)
        btnPinRequired = view.findViewById(R.id.btnPinRequired)

        val btn1 = view.findViewById<Button>(R.id.btn1)
        val btn2 = view.findViewById<Button>(R.id.btn2)
        val btn3 = view.findViewById<Button>(R.id.btn3)
        val btn4 = view.findViewById<Button>(R.id.btn4)
        val btn5 = view.findViewById<Button>(R.id.btn5)
        val btn6 = view.findViewById<Button>(R.id.btn6)
        val btn7 = view.findViewById<Button>(R.id.btn7)
        val btn8 = view.findViewById<Button>(R.id.btn8)

        buttons = arrayOf(btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8)
        buttons.forEachIndexed { idx, btn ->
            btn.setOnClickListener {
                pinLength = idx + 1
               showHideEdtPins(pinLength)
            }
        }

        val p1 = view.findViewById<EditText>(R.id.p1)
        val p2 = view.findViewById<EditText>(R.id.p2)
        val p3 = view.findViewById<EditText>(R.id.p3)
        val p4 = view.findViewById<EditText>(R.id.p4)
        val p5 = view.findViewById<EditText>(R.id.p5)
        val p6 = view.findViewById<EditText>(R.id.p6)
        val p7 = view.findViewById<EditText>(R.id.p7)
        val p8 = view.findViewById<EditText>(R.id.p8)

        edtPins = arrayOf(p1, p2, p3, p4, p5, p6, p7, p8)

        textWatchers = arrayOf(
            PinTextWatcher(0, edtPins, activity as EngineerDetailsActivity),
            PinTextWatcher(1, edtPins, activity as EngineerDetailsActivity),
            PinTextWatcher(2, edtPins, activity as EngineerDetailsActivity),
            PinTextWatcher(3, edtPins, activity as EngineerDetailsActivity),
            PinTextWatcher(4, edtPins, activity as EngineerDetailsActivity),
            PinTextWatcher(5, edtPins, activity as EngineerDetailsActivity),
            PinTextWatcher(6, edtPins, activity as EngineerDetailsActivity),
            PinTextWatcher(7, edtPins, activity as EngineerDetailsActivity)
        )
        onKeyListeners = arrayOf(
            PinOnKeyListener(0, edtPins),
            PinOnKeyListener(1, edtPins),
            PinOnKeyListener(2, edtPins),
            PinOnKeyListener(3, edtPins),
            PinOnKeyListener(4, edtPins),
            PinOnKeyListener(5, edtPins),
            PinOnKeyListener(6, edtPins),
            PinOnKeyListener(7, edtPins)
        )

        edtPins.forEachIndexed { index, editText ->
            editText.addTextChangedListener(textWatchers[index])
            editText.setOnKeyListener(onKeyListeners[index])
        }

        btnPinType.check(R.id.btnNoPin)
        llPinRequired.visibility = View.GONE
        btnPinLength.check(buttons[pinLength - 1].id)
        showHideEdtPins(pinLength)

        btnNoPin.setOnClickListener {
            isPinRequired = false
            llPinRequired.visibility = View.GONE
        }

        btnPinRequired.setOnClickListener {
            isPinRequired = true
            llPinRequired.visibility = View.VISIBLE
        }

        btnUpdate.setOnClickListener {
            val pin = getPin(pinLength)
            if (pin != null && pin.size == pinLength) {
                BluetoothLeService.service?.setPin(PINNumber(pinLength, pin))
            }

            Log.d(TAG, "========== Selected Item: ${loopView.selectedItem}")

            val item = loopView.selectedItem
            if (BluetoothLeService?.service?.device?.simType != Util.getSimType( item)) {
                Handler(Looper.getMainLooper()).postDelayed({
                    BluetoothLeService.service?.setSimType(Util.getSimType( item ))
                },
                1000)
            }

            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditSimFragment).commit()
        }

        loopView.setArrayList(arrayListOf(Util.getSimTypeName(SimType.ModemSimTypeUnknown), Util.getSimTypeName(SimType.ModemSimTypeInstallerProvided), Util.getSimTypeName(SimType.ModemSimTypeUserContract), Util.getSimTypeName(SimType.ModemSimTypeUserPAYG)))
        loopView.selectedItem = BluetoothLeService.service?.device?.simType?.ordinal ?: 0
        btnCancel.setOnClickListener {
            (activity as EngineerDetailsActivity).supportFragmentManager.beginTransaction().remove(this@EditSimFragment).commit()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog =  super.onCreateDialog(savedInstanceState)
        if(bottomSheetDialog is BottomSheetDialog){
            bottomSheetDialog.behavior.skipCollapsed = true
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog
    }

    private fun showHideEdtPins(l: Int) {
        val edtTexts: ArrayList<EditText> = ArrayList()
        edtPins.forEachIndexed { i, p ->
            if (i < l) {
                p.visibility = View.VISIBLE
                if (i < edtTexts.size) edtTexts[i] = p
                p.removeTextChangedListener(textWatchers[i])
                textWatchers[i].isLast = i == (l - 1)
                p.addTextChangedListener(textWatchers[i])
            } else {
                p.visibility = View.GONE
            }
        }

        edtTexts.forEachIndexed{i, _ ->
            textWatchers[i].editTexts = edtTexts.toTypedArray()
        }
    }

    private fun getPin(l: Int): IntArray? {
        var pin = IntArray(l)

        for (i in 0 until l) {
            val p = edtPins[i]

            try {
                pin[i] = p.text.toString().toInt()
            } catch (e: Exception) {
                return null
            }
        }

        return pin
    }

    companion object {
        val TAG: String = "EditSimFragment"
    }
}