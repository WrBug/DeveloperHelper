package com.wrbug.developerhelper.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.wrbug.developerhelper.shell.ShellManager

object AccessibilityManager {
    fun startService(context: Context?): Boolean {
        context?.run {
            if (!ShellManager.openAccessibilityService()) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return false
            }
            return true
        }
        return false
    }
}