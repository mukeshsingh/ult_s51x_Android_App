package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.ultrontech.s515liftconfigure.databinding.ActivityTroubleshootingBinding

class TroubleshootingActivity : AppCompatActivity() {
    lateinit var binding: ActivityTroubleshootingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTroubleshootingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.t1.setOnClickListener {
            if (binding.listDetail1.visibility == View.GONE) {
                binding.listDetail1.visibility = View.VISIBLE
                binding.t1.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.down_arrow, theme))
            } else {
                binding.listDetail1.visibility = View.GONE
                binding.t1.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.forword_arrow, theme))
            }
        }
        binding.t2.setOnClickListener {
            if (binding.listDetail2.visibility == View.GONE) {
                binding.listDetail2.visibility = View.VISIBLE
                binding.t2.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.down_arrow, theme))
            } else {
                binding.listDetail2.visibility = View.GONE
                binding.t2.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.forword_arrow, theme))
            }
        }
        binding.t3.setOnClickListener {
            if (binding.listDetail3.visibility == View.GONE) {
                binding.listDetail3.visibility = View.VISIBLE
                binding.t3.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.down_arrow, theme))
            } else {
                binding.listDetail3.visibility = View.GONE
                binding.t3.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.forword_arrow, theme))
            }
        }

        binding.footer.btnHome.setOnClickListener {
            finish()
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }
}