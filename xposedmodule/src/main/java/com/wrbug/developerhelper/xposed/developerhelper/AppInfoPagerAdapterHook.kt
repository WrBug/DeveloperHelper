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
        enforceHook(lpparam)
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
            addAppXposedItem(adapter, packageInfo, applicationInfo, itemInfos)
        }
    }

    private fun addAppXposedItem(
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
            val status = AppXposedProcessDataManager.instance.isAppXposedOpened(packageName).not()
            AppXposedProcessDataManager.instance.setAppXposedStatus(
                packageName,
                status
            )
            val info = XposedHelpers.callMethod(adapter, "findItemById", id)
            if (status) {
                this?.context?.showToast("已开启xposed功能，重启【${label}】后生效")
                setItemInfo(info, "关闭${label}Xposed功能", "点击关闭")
            } else {
                this?.context?.showToast("已关闭xposed功能，重启【${label}】后生效")
                setItemInfo(info, "开启${label}Xposed功能", "点击开启")
            }
            val infoAdapter =
                XposedHelpers.getObjectField(adapter, "adapter")
            XposedHelpers.callMethod(infoAdapter, "notifyDataSetChanged")
        }

        if (AppXposedProcessDataManager.instance.isAppXposedOpened(packageName)) {
            itemInfos.add(
                createItemInfo(
                    id,
                    "关闭${label}Xposed功能",
                    "点击关闭",
                    Color.parseColor("#0288d1"),
                    click
                )
            )
        } else {
            itemInfos.add(
                createItemInfo(
                    id,
                    "开启${label}Xposed功能",
                    "点击开启",
                    Color.parseColor("#0288d1"),
                    click
                )
            )
        }
    }

    private fun enforceHook(lpparam: XC_LoadPackage.LoadPackageParam) {
        hookMethod(
            lpparam.classLoader,
            "setEnforceType"
        ) {
            "hook setEnforceType".xposedLog()
            val thisObject = thisObject ?: return@hookMethod
            val enforceItem = XposedHelpers.getObjectField(thisObject, "enforceItem")
            val itemInfos =
                XposedHelpers.getObjectField(thisObject, "itemInfos") as ArrayList<Any>
            val index = itemInfos.indexOf(enforceItem)
            if (index < 0) {
                return@hookMethod
            }
            val type = args[0]
            val name = XposedHelpers.callMethod(type, "name") as String
            val apkInfo = XposedHelpers.getObjectField(thisObject, "apkInfo")
            val applicationInfo = XposedHelpers.getObjectField(apkInfo, "applicationInfo")
            val packageName =
                XposedHelpers.getObjectField(applicationInfo, "packageName") as String
            "加固类型：$name 添加脱壳按钮".xposedLog()
            val data = ProcessDataCreator.get(DumpDexListProcessData::class.java)
            val list = data.getData()
            val open = list?.contains(packageName) ?: false
            val str = if (open) "关闭脱壳" else "点击脱壳"
            val item =
                enforceItem.javaClass.getConstructor(String::class.java, Any::class.java)
                    .newInstance("脱壳(Xposed)", str)
            XposedHelpers.callMethod(item, "setOnClickListener", View.OnClickListener { v ->
                val data = ProcessDataCreator.get(DumpDexListProcessData::class.java)
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

        XposedHelpers.findAndHookMethod(
            "com.wrbug.developerhelper.ui.widget.appsettingview.AppSettingView",
            lpparam.classLoader,
            "initView",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val view =
                        XposedHelpers.getObjectField(param?.thisObject, "exportDexBtn") as View?
                    view?.run {
                        visibility = View.VISIBLE
                    }
                }
            })
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

    private fun showDumpDialog(packageName: String, context: Context?) {
        context?.apply {
            AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("脱壳需要重新启动应用，是否继续")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定") { _, _ ->
                    val data = ProcessDataCreator.get(DumpDexListProcessData::class.java)
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