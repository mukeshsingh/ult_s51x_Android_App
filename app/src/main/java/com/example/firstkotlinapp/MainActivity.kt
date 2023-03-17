package com.example.firstkotlinapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.example.firstkotlinapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomsheetFragment = BtmSheet1Fragment()
        val bottomsheetFragment2 = BtmSheetFragment2()
        val bottomsheetFragment3 = BtmSheetFragment3()
        val bottomsheetFragment4 = BtmSheetFragment4()
        val bottomsheetFragment5 = BtmSheetFragment5()





        val welcomeText = findViewById<TextView>(R.id.vishId)
        val inputField = findViewById<EditText>(R.id.maTextInput)
        val submitBtn = findViewById<Button>(R.id.maSubmit)
        val nextBtn = findViewById<Button>(R.id.button)
        var enteredName = ""

        var showBtmSheet = findViewById<Button>(R.id.button2)
        var showBtmSheet2 = findViewById<Button>(R.id.button3)
        var showBtmSheet3 = findViewById<Button>(R.id.button8)
        var showBtmSheet4 = findViewById<Button>(R.id.button9)
        var showBtmSheet5 = findViewById<Button>(R.id.button10)




        showBtmSheet.setOnClickListener {
            bottomsheetFragment.show(supportFragmentManager, "BtmSheet1Fragment")
        }
        showBtmSheet2.setOnClickListener {
            bottomsheetFragment2.show(supportFragmentManager, "BtmSheetFragment2")
        }
        showBtmSheet3.setOnClickListener {
            bottomsheetFragment3.show(supportFragmentManager, "BtmSheetFragment3")
        }
        showBtmSheet4.setOnClickListener {
            bottomsheetFragment4.show(supportFragmentManager, "BtmSheetFragment4")
        }
        showBtmSheet5.setOnClickListener {
            bottomsheetFragment5.show(supportFragmentManager, "BtmSheetFragment5")
        }


        submitBtn.setOnClickListener {
            enteredName = inputField.text.toString()
            if (enteredName == ""){
                 nextBtn.visibility = INVISIBLE
            }else {
                val message = "ULTRON welcomes $enteredName"
                welcomeText.text = message
                inputField.text.clear()
                nextBtn.visibility = VISIBLE
            }
        }
    }
}