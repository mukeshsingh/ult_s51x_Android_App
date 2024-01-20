package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.ultrontech.s515liftconfigure.databinding.ActivitySelectLiftTypeBinding

class SelectLiftTypeActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivitySelectLiftTypeBinding
    private var liftType = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectLiftTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.stairLift.setOnClickListener {
            liftType = "stairLift"

            binding.imgStairLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.stair_lift_dark, this.theme))
            binding.imgInclinedLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.inclined_lift, this.theme))
            binding.imgVerticalLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.vertical_lift, this.theme))
            binding.imgElevator.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.elevator, this.theme))

            binding.stairLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_enable, this.theme)
            binding.inclinedLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.verticalLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.elevatorBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
        }

        binding.inclinedLift.setOnClickListener {
            liftType = "inclinedLift"

            binding.imgStairLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.stair_lift, this.theme))
            binding.imgInclinedLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.inclined_lift_dark, this.theme))
            binding.imgVerticalLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.vertical_lift, this.theme))
            binding.imgElevator.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.elevator, this.theme))

            binding.stairLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.inclinedLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_enable, this.theme)
            binding.verticalLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.elevatorBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
        }

        binding.verticalLift.setOnClickListener {
            liftType = "verticalLift"

            binding.imgStairLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.stair_lift, this.theme))
            binding.imgInclinedLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.inclined_lift, this.theme))
            binding.imgVerticalLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.vertical_lift_dark, this.theme))
            binding.imgElevator.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.elevator, this.theme))

            binding.stairLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.inclinedLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.verticalLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_enable, this.theme)
            binding.elevatorBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
        }

        binding.elevator.setOnClickListener {
            liftType = "elevator"

            binding.imgStairLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.stair_lift, this.theme))
            binding.imgInclinedLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.inclined_lift, this.theme))
            binding.imgVerticalLift.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.vertical_lift, this.theme))
            binding.imgElevator.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.elevator_dark, this.theme))

            binding.stairLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.inclinedLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.verticalLiftBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_disable, this.theme)
            binding.elevatorBg.background =
                ResourcesCompat.getDrawable(resources, R.drawable.rounded_corner_bg_right_enable, this.theme)
        }

        binding.btnSearch.setOnClickListener {
            S515LiftConfigureApp.profileStore.selectedLiftType = liftType
            val intent = Intent(this, FindLiftActivity::class.java)
            startActivity(intent)
        }

        binding.footer.btnHome.setOnClickListener {
            var intent = Intent(this, MyProductsActivity::class.java)
            if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
                intent = Intent(this, EngineerHomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding.footer.btnBack.setOnClickListener {
            finish()
        }

        // ****************** Option Menu Start ******************
        binding.toolbar.optionBtn.setOnClickListener {
            if (binding.optionMenu.llOptionMenu.visibility == View.GONE) {
                binding.optionMenu.llOptionMenu.visibility = View.VISIBLE
            } else {
                binding.optionMenu.llOptionMenu.visibility = View.GONE
            }
        }

        binding.optionMenu.llMenuAccount.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            val intent = Intent(this@SelectLiftTypeActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@SelectLiftTypeActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@SelectLiftTypeActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@SelectLiftTypeActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }
}