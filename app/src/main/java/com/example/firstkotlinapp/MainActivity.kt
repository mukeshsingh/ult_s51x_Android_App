package com.example.firstkotlinapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.status_testerscreen)
        val bottomsheetFragment = BtmSheet1Fragment()
        val logoutFragmentSheet = LogoutFragment()
        val bottomsheetFragment3 = BtmSheetFragment3()
        val bottomsheetFragment4 = BtmSheetFragment4()
        val bottomsheetFragment5 = BtmSheetFragment5()
        val editSimFragment = EditSimFragment()
        val loginPinFragment = LoginPinFragment()






//        val welcomeText = findViewById<TextView>(R.id.vishId)
//        val inputField = findViewById<EditText>(R.id.maTextInput)
//        val submitBtn = findViewById<Button>(R.id.maSubmit)
//        val nextBtn = findViewById<Button>(R.id.button)
//        var enteredName = ""
//
//        var showBtmSheet = findViewById<Button>(R.id.button2)
//        var showBtmSheet2 = findViewById<Button>(R.id.button3)
//        var showBtmSheet3 = findViewById<Button>(R.id.button8)
//        var showBtmSheet4 = findViewById<Button>(R.id.button9)
//        var showBtmSheet5 = findViewById<Button>(R.id.button10)
//        var showEditSimFragment = findViewById<Button>(R.id.button15)

        var isLoggedIn = true

        var openLift = findViewById<CardView>(R.id.cvStairLift)
        var findLift = findViewById<CardView>(R.id.cvFindLift)
        var userProfile = findViewById<CardView>(R.id.cvProfile)
        var enterLoginPin = findViewById<CardView>(R.id.cvLogin)
        var openLogout = findViewById<CardView>(R.id.cvLogin)





//        showBtmSheet.setOnClickListener {
//            bottomsheetFragment.show(supportFragmentManager, "BtmSheet1Fragment")
//        }

//        showBtmSheet3.setOnClickListener {
//            bottomsheetFragment3.show(supportFragmentManager, "BtmSheetFragment3")
//        }
//        showBtmSheet4.setOnClickListener {
//            bottomsheetFragment4.show(supportFragmentManager, "BtmSheetFragment4")
//        }
//        showBtmSheet5.setOnClickListener {
//            bottomsheetFragment5.show(supportFragmentManager, "BtmSheetFragment5")
//        }
//        showEditSimFragment.setOnClickListener {
//            editSimFragment.show(supportFragmentManager, "EditSimFragment")
//        }


//        submitBtn.setOnClickListener {
//            enteredName = inputField.text.toString()
//            if (enteredName == ""){
//                nextBtn.visibility = INVISIBLE
//            }else {
//                val message = "ULTRON welcomes $enteredName"
//                welcomeText.text = message
//                inputField.text.clear()
//                nextBtn.visibility = VISIBLE
//            }
//        }


        openLift.setOnClickListener{

            val intent = Intent(this, EngineerDetails::class.java)
            startActivity(intent)
        }

        findLift.setOnClickListener{

            val intent = Intent(this, FoundLift::class.java)
            startActivity(intent)
        }

        userProfile.setOnClickListener{

            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        enterLoginPin.setOnClickListener {
            if (isLoggedIn){
                openLogout.setOnClickListener {
                logoutFragmentSheet.show(supportFragmentManager, "LogoutFragment")
     }
            }else{
                loginPinFragment.show(supportFragmentManager, "LoginPinFragment")
            }
      }


    }

}