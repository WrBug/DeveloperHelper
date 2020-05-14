package com.wrbug.developerhelper.xposed

import com.wrbug.developerhelper.ipc.processshare.manager.AppXposedProcessDataManager
import com.wrbug.developerhelper.xposed.developerhelper.DeveloperHelper
import com.wrbug.developerhelper.xposed.dumpdex.Dump
import com.wrbug.developerhelper.ipc.processshare.GlobalConfigProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataCreator
import com.wrbug.developerhelper.ipc.processshare.manager.GlobalConfigProcessDataManager
import com.wrbug.developerhelper.xposed.util.ApplicationHelper
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
        val packageName = lpparam.packageName
        if (packageName == SELF_PACKAGE_NAME) {
            "hook 易开发".xposedLog()
            DeveloperHelper.init(lpparam)
            return
        }

        if (GlobalConfigProcessDataManager.isXposedOpen()) {
            "xposed已关闭".xposedLog()
            return
        }
        if (!AppXposedProcessDataManager.isAppXposedOpened(packageName)) {
            "应用未开启xposed".xposedLog()
            return
        }
        ApplicationHelper.hook(lpparam) {
            "hooked ${javaClass.name}".xposedLog()
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
