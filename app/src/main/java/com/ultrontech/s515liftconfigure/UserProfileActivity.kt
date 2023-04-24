package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import androidx.activity.addCallback
import com.ultrontech.s515liftconfigure.models.ProfileStore

class UserProfileActivity : AppCompatActivity() {
    private lateinit var profileName: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        profileName = findViewById(R.id.edt_profile_name)
        profileName.setText(S515LiftConfigureApp.profileStore.userName)

        onBackPressedDispatcher.addCallback {
            S515LiftConfigureApp.profileStore.update(profileName.text.toString())
            finish()
        }
    }
}