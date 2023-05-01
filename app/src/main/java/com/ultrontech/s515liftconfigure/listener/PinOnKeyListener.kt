package com.ultrontech.s515liftconfigure.listener

import android.view.KeyEvent
import android.view.View
import android.view.View.OnKeyListener
import android.widget.EditText

class PinOnKeyListener internal constructor(private var currentIndex: Int, var editTexts: Array<EditText>): OnKeyListener{
    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL && event.action === KeyEvent.ACTION_DOWN) {
            if (editTexts[currentIndex].text.toString().isEmpty() && currentIndex != 0) {
                editTexts[currentIndex - 1].requestFocus()
            }
        }

        return false
    }
}