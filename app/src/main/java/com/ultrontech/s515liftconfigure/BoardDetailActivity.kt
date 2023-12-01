package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ultrontech.s515liftconfigure.databinding.ActivityBoardDetailBinding

class BoardDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityBoardDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.llBtnEditJob.setOnClickListener {

        }

        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

//            finish()
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }
}