package com.ultrontech.s515liftconfigure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.ultrontech.s515liftconfigure.bluetooth.ScanDisplayItem
import com.ultrontech.s515liftconfigure.databinding.ActivityAddLiftBinding
import com.ultrontech.s515liftconfigure.fragments.SuccessAddLiftFragment
import com.ultrontech.s515liftconfigure.fragments.UnSuccessAddLiftFragment
import com.ultrontech.s515liftconfigure.models.PINNumber
import com.ultrontech.s515liftconfigure.models.ProfileStore
import com.ultrontech.s515liftconfigure.models.UserLift

class AddLiftActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddLiftBinding
    private lateinit var p1: EditText
    private lateinit var successFragment: SuccessAddLiftFragment
    private lateinit var unSuccessFragment: UnSuccessAddLiftFragment
    private val hideHandler = Handler(Looper.myLooper()!!)
    private lateinit var homeBtn: ImageButton
    private lateinit var backBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddLiftBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val connectButton = findViewById<Button>(R.id.btnConnect)

        p1 = findViewById(R.id.edtPin)
        homeBtn = findViewById(R.id.btn_home)
        backBtn = findViewById(R.id.btn_back)
        successFragment = SuccessAddLiftFragment()
        unSuccessFragment = UnSuccessAddLiftFragment()

        homeBtn.setOnClickListener {
            var intent = Intent(this, MyProductsActivity::class.java)
            if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
                intent = Intent(this, EngineerHomeActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        backBtn.setOnClickListener {
            finish()
        }

        connectButton.setOnClickListener {
            with(S515LiftConfigureApp) {
                val pinStr = "${p1.text}".trim()

                if (pinStr.length == 6 && ProfileStore.AddLiftPin == pinStr) {
                    val accessKey = pinStr.map { it.digitToInt() }.toIntArray()
                    val userLift = lift?.let { lft -> UserLift(liftId = lft.id, liftName = lft.name, accessKey = PINNumber(6, accessKey)) }

                    if (userLift != null) {
                        if (profileStore.hasEngineerCapability) userLift.liftType = profileStore.selectedLiftType
                        profileStore.add(userLift)
                    }

                    successFragment.show(supportFragmentManager, "SuccessAddLiftFragment")
                    hideHandler.postDelayed(hideSuccess, AUTO_HIDE_DELAY_MILLIS.toLong())
                } else {
                    unSuccessFragment.show(supportFragmentManager, "UnSuccessAddLiftFragment")
                    hideHandler.postDelayed(hideUnSuccess, AUTO_HIDE_DELAY_MILLIS.toLong())
                }
            }
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

            val intent = Intent(this@AddLiftActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@AddLiftActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@AddLiftActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@AddLiftActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }

    private val hideSuccess = Runnable {
        supportFragmentManager.beginTransaction().remove(successFragment).commit()
        var intent = Intent(this, MyProductsActivity::class.java)
        if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
            intent = Intent(this, EngineerHomeActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private val hideUnSuccess = Runnable {
        supportFragmentManager.beginTransaction().remove(unSuccessFragment).commit()
        this.finish()
    }

    companion object{
        var lift: ScanDisplayItem? = null
        private const val AUTO_HIDE_DELAY_MILLIS = 1000
    }
}