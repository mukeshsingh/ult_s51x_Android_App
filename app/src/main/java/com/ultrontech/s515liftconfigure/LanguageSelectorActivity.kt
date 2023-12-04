package com.ultrontech.s515liftconfigure

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ultrontech.s515liftconfigure.databinding.ActivityLanguageSelectorBinding
import com.ultrontech.s515liftconfigure.models.SimType
import com.ultrontech.s515liftconfigure.models.Util

class LanguageSelectorActivity : AppCompatActivity() {
    lateinit var binding: ActivityLanguageSelectorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguageSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loopViewLanguage.setArrayList(arrayListOf("English"))

        binding.btnConfirmLanguage.setOnClickListener {
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