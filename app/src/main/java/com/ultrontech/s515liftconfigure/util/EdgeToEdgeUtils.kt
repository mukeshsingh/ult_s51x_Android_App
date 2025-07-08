package com.ultrontech.s515liftconfigure.util

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

object EdgeToEdgeUtils {
    
    fun enableEdgeToEdge(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        } else {
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        activity.window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }
    }
    
    fun handleRootWindowInsets(rootView: View, applyTopInset: Boolean = true, applyBottomInset: Boolean = true) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            val topInset = if (applyTopInset) insets.top else 0
            val bottomInset = if (applyBottomInset) insets.bottom else 0
            
            view.updatePadding(
                left = insets.left,
                top = topInset,
                right = insets.right,
                bottom = bottomInset
            )
            
            WindowInsetsCompat.CONSUMED
        }
    }
    
    fun handleToolbarInsets(toolbar: View) {
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
            }
            
            windowInsets
        }
    }
    
    fun handleBottomNavigationInsets(bottomNavigation: View) {
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
            }
            
            windowInsets
        }
    }
    
    fun handleKeyboardInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val keyboardInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            v.updatePadding(
                bottom = maxOf(keyboardInsets.bottom, systemBarsInsets.bottom)
            )
            
            windowInsets
        }
    }
}