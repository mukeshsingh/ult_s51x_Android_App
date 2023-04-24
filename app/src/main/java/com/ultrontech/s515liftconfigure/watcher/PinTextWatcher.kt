package com.ultrontech.s515liftconfigure.watcher

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.ultrontech.s515liftconfigure.hideKeyboard

class PinTextWatcher internal constructor(private var currentIndex: Int, private var editTexts: Array<EditText>, private var context: Context): TextWatcher {
    private var isFirst = false
    private  var isLast: Boolean = false
    private var newTypedString = ""

    init {
        if (currentIndex == 0) {
            isFirst = true
        } else if (currentIndex == editTexts.size - 1) {
            this.isLast = true
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        newTypedString = s.subSequence(start, start + count).toString().trim { it <= ' ' }
    }

    override fun afterTextChanged(s: Editable?) {
        var text = newTypedString

        /* Detect paste event and set first char */
        if (text.length > 1) text = text[0].toString() // TODO: We can fill out other EditTexts
        editTexts[currentIndex].removeTextChangedListener(this)
        editTexts[currentIndex].setText(text)
        editTexts[currentIndex].setSelection(text.length)
        editTexts[currentIndex].addTextChangedListener(this)
        if (text.length == 1) moveToNext() else if (text.isEmpty()) moveToPrevious()
    }

    private fun moveToNext() {
        if (!isLast) editTexts[currentIndex + 1].requestFocus()
        if (isAllEditTextsFilled() && isLast) { // isLast is optional
            editTexts[currentIndex].clearFocus()

            context.hideKeyboard(editTexts[currentIndex])
        }
    }

    private fun moveToPrevious() {
        if (!isFirst) editTexts[currentIndex - 1].requestFocus()
    }

    private fun isAllEditTextsFilled(): Boolean {
        for (editText in editTexts)
            if (editText.text.toString().trim { it <= ' ' }.isEmpty()) return false

        return true
    }
}