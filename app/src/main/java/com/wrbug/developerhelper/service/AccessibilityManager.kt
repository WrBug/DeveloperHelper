package com.wrbug.developerhelper.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.showToast
import com.wrbug.developerhelper.commonutil.shell.Callback
import com.wrbug.developerhelper.commonutil.shell.ShellManager

object AccessibilityManager {
    fun startService(context: Context?, callback: Callback<Boolean>? = null) {
        context?.run {
            ShellManager.openAccessibilityService(object : Callback<Boolean> {
                override fun onSuccess(data: Boolean) {
                    if (!data) {
                        showToast(getString(R.string.please_open_accessbility_service))
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    callback?.onSuccess(data)
                }

                override fun onFailed(msg: String) {
                    callback?.onFailed(msg)
                }

            })
        }
    }
}