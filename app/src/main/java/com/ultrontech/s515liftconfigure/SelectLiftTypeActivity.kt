package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ultrontech.s515liftconfigure.databinding.ActivitySelectLiftTypeBinding

class SelectLiftTypeActivity : AppCompatActivity() {
    lateinit var binding: ActivitySelectLiftTypeBinding
    private var liftType = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectLiftTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stairLift.setOnClickListener {
            liftType = "stairLift"
            binding.stairLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_enable, this.theme)
            binding.inclinedLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.verticalLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.elevatorBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
        }

        binding.inclinedLift.setOnClickListener {
            liftType = "inclinedLift"
            binding.stairLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.inclinedLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_enable, this.theme)
            binding.verticalLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.elevatorBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
        }

        binding.verticalLift.setOnClickListener {
            liftType = "verticalLift"
            binding.stairLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.inclinedLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.verticalLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_enable, this.theme)
            binding.elevatorBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
        }

        binding.elevator.setOnClickListener {
            liftType = "elevator"
            binding.stairLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.inclinedLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.verticalLiftBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.elevatorBg.background =
                resources.getDrawable(R.drawable.rounded_corner_bg_right_enable, this.theme)
        }

        binding.btnSearch.setOnClickListener {
            S515LiftConfigureApp.profileStore.selectedLiftType = liftType
            val intent = Intent(this, FindLiftActivity::class.java)
            startActivity(intent)
        }

        binding.footer.btnHome.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }
    }
}