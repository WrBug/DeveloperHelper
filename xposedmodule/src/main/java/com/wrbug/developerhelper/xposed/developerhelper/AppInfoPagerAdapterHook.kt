package com.wrbug.developerhelper.xposed.developerhelper

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.basecommon.showToast
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataCreator
import com.wrbug.developerhelper.ipc.processshare.manager.AppXposedProcessDataManager
import com.wrbug.developerhelper.ipc.processshare.manager.DumpDexListProcessDataManager
import com.wrbug.developerhelper.ipc.processshare.manager.GlobalConfigProcessDataManager
import com.wrbug.developerhelper.xposed.xposedLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object AppInfoPagerAdapterHook {
    private var itemInfoClass: Class<*>? = null
    private fun hookMethod(
        classLoader: ClassLoader,
        method: String,
        after: XC_MethodHook.MethodHookParam.() -> Unit
    ) {
        XposedBridge.hookAllMethods(
            classLoader.loadClass("com.wrbug.developerhelper.ui.activity.hierachy.AppInfoPagerAdapter"),
            method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    param?.after()
                }
            })
    }

    fun start(lpparam: XC_LoadPackage.LoadPackageParam) {
        headerItemHook(lpparam)
    }

    private fun headerItemHook(lpparam: XC_LoadPackage.LoadPackageParam) {

        hookMethod(lpparam.classLoader, "headerItemHook") {
            "hook headerItemHook".xposedLog()
            val adapter = thisObject
            val packageInfo = args[0] as PackageInfo
            val applicationInfo = args[1] as ApplicationInfo
            val itemInfos = args[2] as ArrayList<Any>
            itemInfoClass = itemInfos[0].javaClass
            addAppXposedItem(lpparam, adapter, packageInfo, applicationInfo, itemInfos)
        }
    }

    private fun addAppXposedItem(
        lpparam: XC_LoadPackage.LoadPackageParam,
        adapter: Any,
        packageInfo: PackageInfo,
        applicationInfo: ApplicationInfo,
        itemInfos: java.util.ArrayList<Any>
    ) {
        if (GlobalConfigProcessDataManager.instance.isXposedOpen().not()) {
            return
        }
        val packageName = packageInfo.packageName
        val id = "appXposed"
        val context = XposedHelpers.getObjectField(adapter, "context") as? Context
        val label = context?.packageManager?.getApplicationLabel(applicationInfo)
        val click: View?.() -> Unit = {
            val intent = Intent(
                context,
                XposedHelpers.findClass(
                    "com.wrbug.developerhelper.ui.activity.xposed.appxposedmodulemanager.AppXposedModuleManagerActivity",
                    lpparam.classLoader
                )
            )
            context?.startActivity(intent)
        }
        itemInfos.add(
            createItemInfo(
                id,
                "${label}Xposed模块管理",
                "点击设置",
                Color.parseColor("#0288d1"),
                click
            )
        )

    }


    private fun createItemInfo(
        id: String,
        title: String,
        text: String,
        color: Int,
        onclick: View?.() -> Unit
    ): Any {
        return XposedHelpers.newInstance(itemInfoClass, title, text, object : View.OnClickListener {
            override fun onClick(v: View?) {
                v.onclick()
            }

        }).apply {
            XposedHelpers.callMethod(this, "setId", id)
            XposedHelpers.callMethod(this, "setTextColor", color)
        }
    }

    private fun setItemInfo(
        info: Any,
        title: String? = null,
        content: String? = null
    ) {
        XposedHelpers.callMethod(info, "setTitle", title)
        XposedHelpers.callMethod(info, "setContent", content)
    }
}