package com.ultrontech.s515liftconfigure.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ultrontech.s515liftconfigure.HomeActivity
import com.ultrontech.s515liftconfigure.R
import com.ultrontech.s515liftconfigure.S515LiftConfigureApp
import com.ultrontech.s515liftconfigure.listener.PinOnKeyListener
import com.ultrontech.s515liftconfigure.watcher.PinTextWatcher

class LoginPinFragment : BottomSheetDialogFragment() {
    private lateinit var homeActivity: HomeActivity
    private lateinit var p1: EditText
    private lateinit var p2: EditText
    private lateinit var p3: EditText
    private lateinit var p4: EditText
    private lateinit var p5: EditText
    private lateinit var p6: EditText
    private lateinit var p7: EditText
    private lateinit var p8: EditText
    private lateinit var editTexts: Array<EditText>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        homeActivity = (activity as HomeActivity)

        val view = inflater.inflate(R.layout.fragment_login_pin, container, false)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val cancelButton = view.findViewById<Button>(R.id.btnCancel)

        p1 = view.findViewById(R.id.edtPin1)
        p2 = view.findViewById(R.id.edtPin2)
        p3 = view.findViewById(R.id.edtPin3)
        p4 = view.findViewById(R.id.edtPin4)
        p5 = view.findViewById(R.id.edtPin5)
        p6 = view.findViewById(R.id.edtPin6)
        p7 = view.findViewById(R.id.edtPin7)
        p8 = view.findViewById(R.id.edtPin8)

        editTexts = arrayOf(p1, p2, p3, p4, p5, p6, p7, p8)

        loginButton.setOnClickListener {
            with(S515LiftConfigureApp) {
                val result = profileStore.login("${p1.text}${p2.text}${p3.text}${p4.text}${p5.text}${p6.text}${p7.text}${p8.text}")

                if (result) {
                    (activity as HomeActivity).supportFragmentManager.beginTransaction()
                        .remove(this@LoginPinFragment).commit()
                    (activity as HomeActivity).loginChanged()
                }
            }
        }

        p1.addTextChangedListener(PinTextWatcher(0, editTexts, activity as HomeActivity));
        p2.addTextChangedListener(PinTextWatcher(1, editTexts, activity as HomeActivity));
        p3.addTextChangedListener(PinTextWatcher(2, editTexts, activity as HomeActivity));
        p4.addTextChangedListener(PinTextWatcher(3, editTexts, activity as HomeActivity));
        p5.addTextChangedListener(PinTextWatcher(4, editTexts, activity as HomeActivity));
        p6.addTextChangedListener(PinTextWatcher(5, editTexts, activity as HomeActivity));
        p7.addTextChangedListener(PinTextWatcher(6, editTexts, activity as HomeActivity));
        p8.addTextChangedListener(PinTextWatcher(7, editTexts, activity as HomeActivity));

        p1.setOnKeyListener(PinOnKeyListener(0, editTexts))
        p2.setOnKeyListener(PinOnKeyListener(1, editTexts))
        p3.setOnKeyListener(PinOnKeyListener(2, editTexts))
        p4.setOnKeyListener(PinOnKeyListener(3, editTexts))
        p5.setOnKeyListener(PinOnKeyListener(4, editTexts))
        p6.setOnKeyListener(PinOnKeyListener(5, editTexts))
        p7.setOnKeyListener(PinOnKeyListener(6, editTexts))
        p8.setOnKeyListener(PinOnKeyListener(7, editTexts))

        cancelButton.setOnClickListener {
            (activity as HomeActivity).supportFragmentManager.beginTransaction().remove(this@LoginPinFragment).commit()
        }
        return view
    }
}