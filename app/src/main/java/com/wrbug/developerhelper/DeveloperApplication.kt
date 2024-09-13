package com.wrbug.developerhelper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Process
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.internal.DefaultsFactory
import com.wrbug.developerhelper.base.BaseApp
import com.wrbug.developerhelper.commonutil.ProcessUtil
import com.wrbug.developerhelper.commonutil.print
import com.wrbug.developerhelper.ipcserver.IpcManager
import com.wrbug.developerhelper.ui.activity.main.MainActivity
import com.wrbug.developerhelper.util.AppStatusRegister

class DeveloperApplication : BaseApp() {
    companion object {

        private lateinit var instance: DeveloperApplication
        fun getInstance(): DeveloperApplication {
            return instance
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        XLog.init(
            LogConfiguration.Builder().logLevel(LogLevel.ALL).tag("developerHelper.print-->")
                .build(),
            DefaultsFactory.createPrinter()
        )
        registerIpcServer()
        BaseModule.init(this)
        registerLifecycle()
        AppStatusRegister.init(this)
    }

    private fun registerIpcServer() {
        val name = ProcessUtil.readProcName(Process.myPid())
        if (name != "$packageName:floatWindow") {
            "ignore registerIpcServer : $name".print()
            return
        }
        IpcManager.init()
        "registerIpcServer: ${Process.myPid()}".print()
    }

    private fun registerLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var count = 0
            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityStarted(activity: Activity) {
                count++
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
                count--
                if (count == 0 && activity is MainActivity) {
                    activity.finish()
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

        })
    }
}
