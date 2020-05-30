package com.wrbug.developerhelper.xposed

import android.os.Process
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.commonutil.ProcessUtil
import com.wrbug.developerhelper.commonutil.ShellUtils
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.ipc.processshare.manager.AppXposedProcessDataManager
import com.wrbug.developerhelper.ipc.processshare.manager.GlobalConfigProcessDataManager
import com.wrbug.developerhelper.xposed.developerhelper.DeveloperHelper
import com.wrbug.developerhelper.xposed.dumpdex.Dump
import com.wrbug.developerhelper.xposed.util.ApplicationHelper
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File

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
//        ("进程：${Process.myPid()}, ${ProcessUtil.readProcName()}, ${Shell.SH.run("cat /data/data/com.wrbug.developerhelper/shared_prefs/ipc_app_xposed_config.xml").toJson()}" +
//                ", ${Shell.SU.run("cat /data/data/com.wrbug.developerhelper/shared_prefs/ipc_app_xposed_config.xml").toJson()}").xposedLog(
//            "XPOSED_PROCESS"
//        )
        if (GlobalConfigProcessDataManager.instance.isXposedOpen()) {
            "xposed已关闭".xposedLog()
            return
        }
        if (!AppXposedProcessDataManager.instance.isAppXposedOpened(packageName)) {
            "应用${packageName}:${lpparam.processName}未开启xposed".xposedLog()
            return
        }
        ApplicationHelper.hook(lpparam) {
            "hooked ${javaClass.name}".xposedLog()
        }
        Dump.init(lpparam)
    }

    companion object {
        const val SELF_PACKAGE_NAME = "com.wrbug.developerhelper"
        fun log(t: Throwable) {
            XposedBridge.log(t)
        }
    }
}
