package com.ultrontech.s515liftconfigure

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.ultrontech.s515liftconfigure.fragments.EditSimFragment
import com.ultrontech.s515liftconfigure.fragments.LoginPinFragment
import com.ultrontech.s515liftconfigure.fragments.LogoutFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.status_testerscreen)
        val logoutFragmentSheet = LogoutFragment()
        val editSimFragment = EditSimFragment()
        val loginPinFragment = LoginPinFragment()

        var isLoggedIn = true

        var openLift = findViewById<CardView>(R.id.cvStairLift)
        var findLift = findViewById<CardView>(R.id.cvFindLift)
        var userProfile = findViewById<CardView>(R.id.cvProfile)
        var enterLoginPin = findViewById<CardView>(R.id.cvLogin)
        var openLogout = findViewById<CardView>(R.id.cvLogin)

        openLift.setOnClickListener {

            val intent = Intent(this, EngineerDetailsActivity::class.java)
            startActivity(intent)
        }

        findLift.setOnClickListener {

            val intent = Intent(this, FindLiftActivity::class.java)
            startActivity(intent)
        }

        userProfile.setOnClickListener {

            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        enterLoginPin.setOnClickListener {
            if (isLoggedIn) {
                openLogout.setOnClickListener {
                    logoutFragmentSheet.show(supportFragmentManager, "LogoutFragment")
                }
            } else {
                loginPinFragment.show(supportFragmentManager, "LoginPinFragment")
            }
        }


    }

}