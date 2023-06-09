package com.ultrontech.s515liftconfigure

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.ultrontech.s515liftconfigure.models.ProfileStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class S515LiftConfigureApp: Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this

        json = Json { ignoreUnknownKeys = true }

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        profileStore = ProfileStore()
    }

    fun basicAlert(context: Context, message: String, callback: (() -> Unit)?){
        val builder = AlertDialog.Builder(context)
        with(builder) {
            setTitle("Lifts")
            setMessage(message)
            setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                callback?.invoke()
            })
            show()
        }
    }

    inline fun startCoroutineTimer(delayMillis: Long = 0, repeat: Boolean = false, numberOfRepeat: Long = 0, repeatMillis: Long = 0, crossinline action: () -> Unit) = GlobalScope.launch {
        delay(delayMillis)

        var counter = numberOfRepeat

        if (repeatMillis > 0) {
            while ((repeat && counter > 0 && numberOfRepeat > 0) || (repeat && numberOfRepeat <= 0)) {
                action()
                counter--
                if ((counter > 0 && numberOfRepeat > 0) || numberOfRepeat <= 0) delay(repeatMillis)
            }
        } else {
            action()
        }
    }

    companion object {
        lateinit var instance: S515LiftConfigureApp
        lateinit var sharedPreferences: SharedPreferences
        lateinit var json: Json
        lateinit var profileStore: ProfileStore
        const val TAG = "S515LiftConfigureApp"
        const val KEY_PROFILE_USER_DEVICES = "profile.user.devices"
        const val KEY_PROFILE_USER_NAME ="profile.user.name"
        const val KEY_PROFILE_ENGINEER_LOGGED_IN ="profile.engineer.logged.in"
        const val KEY_PROFILE_USER_USE_BIO ="profile.user.use_bio"
        const val KEY_EMPTY_STRING = ""
        const val KEY_TRUE = true
        const val KEY_FALSE = false
    }
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard() }
}

fun Activity.hideKeyboard() {
    hideKeyboard()
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}