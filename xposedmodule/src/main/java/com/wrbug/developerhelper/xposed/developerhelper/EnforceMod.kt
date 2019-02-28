package com.wrbug.developerhelper.xposed.developerhelper

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataManager
import com.wrbug.developerhelper.xposed.xposedLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

object EnforceMod {
    fun start(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.hookAllMethods(
            lpparam.classLoader.loadClass("com.wrbug.developerhelper.ui.activity.hierachy.AppInfoPagerAdapter"),
            "setEnforceType",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    "hook setEnforceType".xposedLog()
                    val thisObject = param?.thisObject ?: return
                    val enforceItem = XposedHelpers.getObjectField(thisObject, "enforceItem")
                    val itemInfos = XposedHelpers.getObjectField(thisObject, "itemInfos") as ArrayList<Any>
                    val index = itemInfos.indexOf(enforceItem)
                    if (index < 0) {
                        return
                    }
                    val type = param.args[0]
                    val name = XposedHelpers.callMethod(type, "name") as String
                    if (name == "UN_KNOWN") {
                        return
                    }
                    val apkInfo = XposedHelpers.getObjectField(thisObject, "apkInfo")
                    val applicationInfo = XposedHelpers.getObjectField(apkInfo, "applicationInfo")
                    val packageName = XposedHelpers.getObjectField(applicationInfo, "packageName") as String
                    "加固类型：$name 添加脱壳按钮".xposedLog()
                    val data = ProcessDataManager.get(DumpDexListProcessData::class.java)
                    val list = data.getData()
                    val open = list?.contains(packageName) ?: false
                    val str = if (open) "关闭脱壳" else "点击脱壳"
                    val item = enforceItem.javaClass.getConstructor(String::class.java, Any::class.java)
                        .newInstance("脱壳(Xposed)", str)
                    XposedHelpers.callMethod(item, "setOnClickListener", View.OnClickListener { v ->
                        val data = ProcessDataManager.get(DumpDexListProcessData::class.java)
                        val list = data.getData()
                        val open = list?.contains(packageName) ?: false
                        if (open.not()) {
                            showDumpDialog(packageName, v?.context)
                        } else {
                            list?.remove(packageName)
                            data.setData(list ?: arrayListOf())
                            v.context.sendBroadcast(Intent("ACTION_FINISH_HIERACHY_Activity"))
                            Toast.makeText(v.context, "已关闭", Toast.LENGTH_SHORT).show()
                        }
                    })
                    val adapter = XposedHelpers.getObjectField(thisObject, "adapter")
                    XposedHelpers.callMethod(adapter, "addItem", index + 1, item)
                }
            })

        XposedHelpers.findAndHookMethod(
            "com.wrbug.developerhelper.ui.widget.appsettingview.AppSettingView",
            lpparam.classLoader,
            "initView",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val view = XposedHelpers.getObjectField(param?.thisObject, "exportDexBtn") as View?
                    view?.run {
                        visibility = View.VISIBLE
                    }
                }
            })
    }

    private fun showDumpDialog(packageName: String, context: Context?) {
        context?.apply {
            AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("脱壳需要重新启动应用，是否继续")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    val data = ProcessDataManager.get(DumpDexListProcessData::class.java)
                    val list = data.getData() ?: arrayListOf()
                    if (list.contains(packageName).not()) {
                        list.add(packageName)
                        data.setData(list)
                    }
                    "已添加 $packageName".xposedLog()
                    if (Shell.SU.run("am force-stop $packageName").isSuccessful.not()) {
                        Toast.makeText(this, "重启失败", Toast.LENGTH_SHORT).show()
                        list.remove(packageName)
                        data.setData(list)
                        return@setPositiveButton
                    }
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    sendBroadcast(Intent("ACTION_FINISH_HIERACHY_Activity"))
                }.show()
        }
    }
}