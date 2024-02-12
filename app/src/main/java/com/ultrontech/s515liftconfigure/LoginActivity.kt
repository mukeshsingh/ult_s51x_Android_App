package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import com.ultrontech.s515liftconfigure.databinding.ActivityLoginBinding

class LoginActivity : LangSupportBaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.editTextEngineerPin.doOnTextChanged { text, start, before, count ->
            binding.txtDesc.visibility = View.VISIBLE
            binding.llErrMsg.visibility = View.GONE
         }

        binding.btnEngineerPin.setOnClickListener {
            with(S515LiftConfigureApp) {
                val result = profileStore.login(binding.editTextEngineerPin.text.toString().trim())

                Log.d(TAG, "Login result: $result")
                if (result) {
                    val intent = Intent(this@LoginActivity, EngineerHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    binding.txtDesc.visibility = View.GONE
                    binding.llErrMsg.visibility = View.VISIBLE
                }
            }
        }

        binding.toolbar.optionBtn.visibility = View.GONE
    }
}