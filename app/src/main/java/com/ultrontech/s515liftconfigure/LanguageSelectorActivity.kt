package com.ultrontech.s515liftconfigure

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ultrontech.s515liftconfigure.databinding.ActivityLanguageSelectorBinding
import java.util.Locale


class LanguageSelectorActivity : LangSupportBaseActivity() {
    lateinit var binding: ActivityLanguageSelectorBinding
    private lateinit var arrLanguages: List<String>
    private lateinit var arrLanguagesCode: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguageSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arrLanguages = LANGUAGES_MAP.keys.map {
            val id = when(it) {
                "spanish" -> R.string.spanish
                "french" -> R.string.french
                "english" -> R.string.english
                "italian" -> R.string.italian
                "dutch" -> R.string.dutch
                "german" -> R.string.german
                else -> R.string.english
            }
            resources.getString(id)
        }
        arrLanguagesCode = LANGUAGES_MAP.values.toList()

        val local = baseContext.resources.configuration.locales[0]

        binding.loopViewLanguage.setArrayList(ArrayList(arrLanguages))
        binding.loopViewLanguage.selectedItem = arrLanguagesCode.indexOf(local.language)

        binding.btnConfirmLanguage.setOnClickListener {
            val languageCode: String = arrLanguagesCode[binding.loopViewLanguage.selectedItem]

            with(S515LiftConfigureApp) {
                sharedPreferences.edit().putString(KEY_PROFILE_USER_LANGUAGE, languageCode).apply()
            }

            var intent = Intent(this@LanguageSelectorActivity, SplashActivity::class.java)

            if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
                intent = Intent(this@LanguageSelectorActivity, EngineerHomeActivity::class.java)
            } else if (S515LiftConfigureApp.profileStore.hasUserCapability) {
                intent = Intent(this@LanguageSelectorActivity, MyProductsActivity::class.java)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
        }

        binding.footer.btnHome.setOnClickListener {
            var intent = Intent(this@LanguageSelectorActivity, MyProductsActivity::class.java)

            if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
                intent = Intent(this@LanguageSelectorActivity, EngineerHomeActivity::class.java)
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

            val intent = Intent(this@LanguageSelectorActivity, UserProfileActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuLanguage.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@LanguageSelectorActivity, LanguageSelectorActivity::class.java)
            startActivity(intent)
        }
        binding.optionMenu.llMenuTroubleshoot.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
            val intent = Intent(this@LanguageSelectorActivity, TroubleshootingActivity::class.java)
            startActivity(intent)

        }
        binding.optionMenu.llOptionMenu.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE
        }
        binding.optionMenu.llLogout.setOnClickListener {
            binding.optionMenu.llOptionMenu.visibility = View.GONE

            with(S515LiftConfigureApp) {
                profileStore.logout()
                val intent = Intent(this@LanguageSelectorActivity, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
        // ****************** Option Menu End ******************
    }

    companion object{
        val LANGUAGES_MAP = mapOf("dutch" to "nl", "english" to "en", "french" to "fr", "german" to "de", "italian" to "it", "spanish" to "es")
    }
}