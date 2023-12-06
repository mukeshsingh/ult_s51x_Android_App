package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ultrontech.s515liftconfigure.bluetooth.BluetoothLeService
import com.ultrontech.s515liftconfigure.databinding.ActivityUserContactBinding
import com.ultrontech.s515liftconfigure.models.PhoneContact

class UserContactActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserContactBinding
    private var liftId: String? = null
    var phone1: PhoneContact? = null
    var name1: String? = ""
    var phone2: PhoneContact? = null
    var name2: String? = ""
    var phone3: PhoneContact? = null
    var name3: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        liftId = intent.extras?.getString(HomeActivity.INTENT_LIFT_ID)

        phone1 = BluetoothLeService.service?.device?.number1
        name1 = liftId?.let { it1 ->
            S515LiftConfigureApp.profileStore.find(
                it1
            )?.userContact1Name
        }

        phone2 = BluetoothLeService.service?.device?.number2
        name2 = liftId?.let { it1 ->
            S515LiftConfigureApp.profileStore.find(
                it1
            )?.userContact2Name
        }

        phone3 = BluetoothLeService.service?.device?.number3
        name3 = liftId?.let { it1 ->
            S515LiftConfigureApp.profileStore.find(
                it1
            )?.userContact3Name
        }

        binding.contactName1.text = name1
        binding.contactName2.text = name2
        binding.contactName3.text = name3

        binding.phone1.text = phone1?.number
        binding.phone2.text = phone2?.number
        binding.phone3.text = phone3?.number

        if (phone1?.number != null && phone1?.number!!.isNotEmpty()) {
            binding.img1.setImageDrawable(resources.getDrawable(R.drawable.edit_white, theme))
            binding.editContactDetail1.setBackgroundColor(resources.getColor(R.color.blue, theme))
        } else {
            binding.img1.setImageDrawable(resources.getDrawable(R.drawable.plus_sign_white, theme))
            binding.editContactDetail1.setBackgroundColor(resources.getColor(R.color.grey, theme))
        }

        if (phone2?.number != null && phone2?.number!!.isNotEmpty()) {
            binding.img2.setImageDrawable(resources.getDrawable(R.drawable.edit_white, theme))
            binding.editContactDetail2.setBackgroundColor(resources.getColor(R.color.blue, theme))
        } else {
            binding.img2.setImageDrawable(resources.getDrawable(R.drawable.plus_sign_white, theme))
            binding.editContactDetail2.setBackgroundColor(resources.getColor(R.color.grey, theme))
        }

        if (phone3?.number != null && phone3?.number!!.isNotEmpty()) {
            binding.editContactDetail3.setBackgroundColor(resources.getColor(R.color.blue, theme))
            binding.img3.setImageDrawable(resources.getDrawable(R.drawable.edit_white, theme))
        } else {
            binding.img3.setImageDrawable(resources.getDrawable(R.drawable.plus_sign_white, theme))
            binding.editContactDetail3.setBackgroundColor(resources.getColor(R.color.grey, theme))
        }

        binding.editContactDetail1.setOnClickListener {
            ChangeUserContactActivity.phone = phone1
            ChangeUserContactActivity.name = name1
            ChangeUserContactActivity.numberSlot = 1

            val intent = Intent(this, ChangeUserContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.editContactDetail2.setOnClickListener {
            ChangeUserContactActivity.phone = phone2
            ChangeUserContactActivity.name = name2
            ChangeUserContactActivity.numberSlot = 2

            val intent = Intent(this, ChangeUserContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
            startActivity(intent)
        }

        binding.editContactDetail3.setOnClickListener {
            ChangeUserContactActivity.phone = phone3
            ChangeUserContactActivity.name = name3
            ChangeUserContactActivity.numberSlot = 3

            val intent = Intent(this, ChangeUserContactActivity::class.java)
            intent.putExtra(HomeActivity.INTENT_LIFT_ID, liftId)
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

            val intent = Intent(this@UserContactActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@UserContactActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@UserContactActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@UserContactActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }
}