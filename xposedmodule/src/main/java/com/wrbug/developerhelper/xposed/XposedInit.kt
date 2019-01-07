package com.wrbug.developerhelper.xposed

import com.wrbug.developerhelper.xposed.developerhelper.DeveloperHelper
import com.wrbug.developerhelper.xposed.dumpdex.Dump
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * XposedInit
 *
 * @author wrbug
 * @since 2018/3/20
 */
class XposedInit : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == SELF_PACKAGE_NAME) {
            "hook 易开发".xposedLog()
            DeveloperHelper.start(lpparam)
            return
        }
        Dump.start(lpparam)
    }

    companion object {
        const val SELF_PACKAGE_NAME = "com.wrbug.developerhelper"
        fun log(t: Throwable) {
            XposedBridge.log(t)
        }
    }
}
