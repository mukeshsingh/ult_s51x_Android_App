package com.ultrontech.s515liftconfigure

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import changeLang
import com.ultrontech.s515liftconfigure.util.EdgeToEdgeUtils


open class LangSupportBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeUtils.enableEdgeToEdge(this)
    }

    override fun attachBaseContext(newBase: Context) {
        with(S515LiftConfigureApp) {
            var context = newBase

            val langCode = sharedPreferences.getString(KEY_PROFILE_USER_LANGUAGE, null)
            if (langCode != null) {
                context = changeLang(newBase, langCode)
            }

            super.attachBaseContext(context)
        }
    }
}