package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import androidx.activity.addCallback
import com.ultrontech.s515liftconfigure.databinding.ActivityUserProfileBinding
import com.ultrontech.s515liftconfigure.models.ProfileStore

class UserProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edtProfileName.setText(S515LiftConfigureApp.profileStore.userName)

        binding.btnConfirmProfileName.setOnClickListener {
            S515LiftConfigureApp.profileStore.update(binding.edtProfileName.text.toString())
            finish()
        }
        onBackPressedDispatcher.addCallback {
            S515LiftConfigureApp.profileStore.update(binding.edtProfileName.text.toString())
            finish()
        }

        binding.footer.btnHome.setOnClickListener {
            finish()
        }

        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }
}