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
    private lateinit var userBtn: ImageButton
    private lateinit var engineerBtn: ImageButton
    private var viewIndex: Int = 0
    private var itemList: ArrayList<View> = ArrayList()
    private val hideHandler = Handler(Looper.myLooper()!!)

    private val showRunnable = Runnable {
        var pV = if (viewIndex > 0) itemList[viewIndex - 1] else null
        var cV = itemList[viewIndex]
        if (Build.VERSION.SDK_INT >= 30 && isFullscreen) {
            cV.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else if (Build.VERSION.SDK_INT >= 30) {
            cV.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
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
        }
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.

        userBtn = binding.imgBtnUser
        engineerBtn = binding.imgBtnEngineer
        userBtn.setOnClickListener {
            val intent = Intent(this, MyProductsActivity::class.java)
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

        itemList.add(f1)
        itemList.add(f2)
        itemList.add(f3)
        itemList.add(f4)
        itemList.add(f5)
        itemList.add(f6)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        hideHandler.removeCallbacks(showRunnable)
        hideHandler.postDelayed(showRunnable, AUTO_HIDE_DELAY_MILLIS.toLong())
    }

    companion object {
        private const val AUTO_HIDE_DELAY_MILLIS = 500
    }
}