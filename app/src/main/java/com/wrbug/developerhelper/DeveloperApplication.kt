package com.wrbug.developerhelper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Looper
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.internal.DefaultsFactory
import com.wrbug.datafinder.startup.LaunchContentProvider
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.basewidgetimport.BaseModule
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread
import com.wrbug.developerhelper.commonwidget.flexibletoast.FlexibleToast
import com.wrbug.developerhelper.ui.activity.main.MainActivity


class DeveloperApplication : BaseApp() {
    companion object {
        private lateinit var instance: DeveloperApplication
        fun getInstance(): DeveloperApplication {
            return instance
        }
    }

    override fun attachBaseContext(base: Context?) {
        LaunchContentProvider.setAutoLaunch(false)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        BaseModule.init(this)
        instance = this
        XLog.init(
            LogConfiguration.Builder().logLevel(LogLevel.ALL).tag("developerHelper.print-->").build(),
            DefaultsFactory.createPrinter()
        )
        releaseAssetsFile()
        registerLifecycle()
    }

    private fun registerLifecycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var count = 0
            override fun onActivityPaused(activity: Activity?) {

            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityStarted(activity: Activity?) {
                count++
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
                count--
                activity?.let {
                    if (count == 0 && activity is MainActivity) {
                        activity.finish()
                    }
                }

            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }

        })
    }


    private fun releaseAssetsFile() {
        thread {
            val inputStream = BaseApp.instance.assets.open("zip.dex")
            val file = File(BaseApp.instance.cacheDir, "zip.dex")
            if (file.exists().not()) {
                file.createNewFile()
            }
            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(inputStream.readBytes())
            fileOutputStream.flush()
            fileOutputStream.close()
        }
    }
}
