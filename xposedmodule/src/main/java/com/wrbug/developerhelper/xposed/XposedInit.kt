package com.wrbug.developerhelper.xposed

import com.wrbug.developerhelper.xposed.developerhelper.DeveloperHelper
import com.wrbug.developerhelper.xposed.dumpdex.Dump
import com.wrbug.developerhelper.xposed.processshare.GlobalConfigProcessData
import com.wrbug.developerhelper.xposed.processshare.ProcessDataManager
import com.wrbug.developerhelper.xposed.viewnode.ViewNode
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
    var configData: GlobalConfigProcessData? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == SELF_PACKAGE_NAME) {
            "hook 易开发".xposedLog()
            DeveloperHelper.init(lpparam)
            return
        }
        if (configData == null) {
            configData = ProcessDataManager.get(GlobalConfigProcessData::class.java)
        }
        if (configData == null) {
            return
        }
        if (configData?.isXposedOpen() == false) {
            "xposed已关闭".xposedLog()
            return
        }
//        ViewNode.inject(lpparam)
        Dump.init(lpparam)
    }

    companion object {
        const val SELF_PACKAGE_NAME = "com.wrbug.developerhelper"
        fun log(t: Throwable) {
            XposedBridge.log(t)
        }
    }
}
