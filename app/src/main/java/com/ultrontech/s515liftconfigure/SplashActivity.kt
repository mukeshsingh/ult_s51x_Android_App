package com.ultrontech.s515liftconfigure

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.ultrontech.s515liftconfigure.databinding.ActivitySplashBinding

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : LangSupportBaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var f1: ImageView
    private lateinit var f2: ImageView
    private lateinit var f3: ImageView
    private lateinit var f4: ImageView
    private lateinit var f5: ImageView
    private lateinit var f6: LinearLayout
    private lateinit var f7: LinearLayout
    private lateinit var userBtn: ImageButton
    private lateinit var engineerBtn: ImageButton
    private var viewIndex: Int = 0
    private var itemList: ArrayList<View> = ArrayList()
    private val hideHandler = Handler(Looper.myLooper()!!)
    private lateinit var arrLanguages: List<String>
    private lateinit var arrLanguagesCode: List<String>

    private val showRunnable = Runnable {
        var pV = if (viewIndex > 0) itemList[viewIndex - 1] else null

        var cV = itemList[viewIndex]
        if (Build.VERSION.SDK_INT >= 30 && isFullscreen) {
            cV.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else if (Build.VERSION.SDK_INT >= 30) {
            cV.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }

        if (viewIndex == 5) {
            with(S515LiftConfigureApp) {
                val lang = sharedPreferences.getString(KEY_PROFILE_USER_LANGUAGE, null)
                if (lang != null) {
                    viewIndex += 1
                    cV = itemList[viewIndex]
                }
            }
        }

        // Delayed display of UI elements
//        supportActionBar?.show()
        if (pV != null) {
//            pV.animate().alpha(0f).duration = 1000
            pV.visibility = View.GONE
        }
        cV.visibility = View.VISIBLE
        cV.animate().alpha(1f).duration = 1000

        viewIndex += 1
        checkHasToRunRunnable()
    }

    private fun checkHasToRunRunnable() {
        if (viewIndex <= 5) {
            if (viewIndex == 5) isFullscreen = false;
            hideHandler.postDelayed(showRunnable, AUTO_HIDE_DELAY_MILLIS.toLong())
        }
    }
    private var isFullscreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (S515LiftConfigureApp.profileStore.hasEngineerCapability) {
            val intent = Intent(this, EngineerHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else if (S515LiftConfigureApp.profileStore.hasUserCapability) {
            val intent = Intent(this, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        arrLanguages = LanguageSelectorActivity.LANGUAGES_MAP.keys.map {
            val id = when(it) {
                "dutch" -> R.string.dutch
                "english" -> R.string.english
                "french" -> R.string.french
                "german" -> R.string.german
                "italian" -> R.string.italian
                "spanish" -> R.string.spanish
                else -> R.string.english
            }
            resources.getString(id)
        }
        arrLanguagesCode = LanguageSelectorActivity.LANGUAGES_MAP.values.toList()

        val local = baseContext.resources.configuration.locales[0]

        binding.loopViewLanguage.setArrayList(ArrayList(arrLanguages))
        binding.loopViewLanguage.selectedItem = arrLanguagesCode.indexOf(local.language)

        binding.btnConfirmLanguage.setOnClickListener {
            val languageCode: String = arrLanguagesCode[binding.loopViewLanguage.selectedItem]

            with(S515LiftConfigureApp) {
                sharedPreferences.edit().putString(KEY_PROFILE_USER_LANGUAGE, languageCode).apply()
            }

            finish();
            S515LiftConfigureApp.instance.showSplashAnimation = false

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0);
                startActivity(intent);
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0);
            }
        }

        binding.imgConfirmLanguage.setOnClickListener {
            val languageCode: String = arrLanguagesCode[binding.loopViewLanguage.selectedItem]

            with(S515LiftConfigureApp) {
                sharedPreferences.edit().putString(KEY_PROFILE_USER_LANGUAGE, languageCode).apply()
            }

            finish();
            S515LiftConfigureApp.instance.showSplashAnimation = false

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else {
                overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0);
                startActivity(intent);
                overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0);
            }
        }

        // Set up the user interaction to manually show or hide the system UI.

        userBtn = binding.imgBtnUser
        engineerBtn = binding.imgBtnEngineer
        userBtn.setOnClickListener {
            with(S515LiftConfigureApp) {
                profileStore.userLogin()
            }
            val intent = Intent(this@SplashActivity, MyProductsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        engineerBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        f1 = binding.f1
        f2 = binding.f2
        f3 = binding.f3
        f4 = binding.f4
        f5 = binding.f5
        f6 = binding.f6
        f7 = binding.f7

        itemList.add(f1)
        itemList.add(f2)
        itemList.add(f3)
        itemList.add(f4)
        itemList.add(f5)
        itemList.add(f6)
        itemList.add(f7)

//        with(S515LiftConfigureApp) {
//            sharedPreferences.edit().remove(KEY_PROFILE_USER_LANGUAGE).commit()
//        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        if (S515LiftConfigureApp.instance.showSplashAnimation) {
            hideHandler.removeCallbacks(showRunnable)
            hideHandler.postDelayed(showRunnable, AUTO_HIDE_DELAY_MILLIS.toLong())
        } else {
            f7.visibility = View.VISIBLE
            S515LiftConfigureApp.instance.showSplashAnimation = true
        }
    }

    companion object {
        private const val AUTO_HIDE_DELAY_MILLIS = 500
    }
}