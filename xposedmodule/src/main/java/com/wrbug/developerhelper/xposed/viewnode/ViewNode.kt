package com.wrbug.developerhelper.xposed.viewnode

import android.app.Activity
import android.content.IntentFilter
import com.wrbug.developerhelper.xposed.util.ApplicationHelper
import com.wrbug.developerhelper.xposed.xposedLog
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.Exception
import java.lang.ref.WeakReference

object ViewNode {
    private val receiverMap = HashMap<String, WeakReference<XposedViewNodeReceiver>>()
    fun inject(lpparam: XC_LoadPackage.LoadPackageParam) {
        "ViewNode inject".xposedLog()
        ApplicationHelper.hook(lpparam) {
            XposedHelpers.findAndHookMethod(
                Activity::class.java.name,
                classLoader,
                "onStart",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        registerReceiver(param?.thisObject as Activity)
                    }
                })

            XposedHelpers.findAndHookMethod(
                Activity::class.java.name,
                classLoader,
                "onStop",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        unRegisterReceiver(param?.thisObject as Activity)
                    }
                })

        }

    }

    private fun registerReceiver(activity: Activity) {
        val receiver = XposedViewNodeReceiver(activity)
        val filter = IntentFilter("ACTION_HIERARCHY_VIEW")
        activity.registerReceiver(receiver, filter)
        receiverMap[activity.javaClass.name] = WeakReference(receiver)
        "注册广播$activity".xposedLog()
    }

    private fun unRegisterReceiver(activity: Activity) {
        receiverMap[activity.javaClass.name]?.get()?.let {
            try {
                activity.unregisterReceiver(it)
            } catch (e: Exception) {

            }
            "取消广播$activity".xposedLog()
        }
    }
}