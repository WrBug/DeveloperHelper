package com.wrbug.developerhelper.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.showToast
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.util.getString
import io.reactivex.rxjava3.core.Single

object AccessibilityManager {
    fun startService(context: Context?): Single<Boolean> {
        return ShellManager.openAccessibilityService().doOnSuccess {
            if (!it) {
                context?.showToast(getString(R.string.please_open_accessbility_service))
            }
        }
    }


    fun startAccessibilitySetting(context: Context?) {
        context ?: return
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}