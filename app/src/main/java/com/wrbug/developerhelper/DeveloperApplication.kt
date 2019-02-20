package com.wrbug.developerhelper

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.internal.DefaultsFactory
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.basewidgetimport.BaseWidget
import com.wrbug.developerhelper.commonutil.CommonUtils
import com.wrbug.developerhelper.mmkv.manager.MMKVManager
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread
import com.wrbug.developerhelper.commonwidget.flexibletoast.FlexibleToast
import com.wrbug.developerhelper.ui.activity.main.MainActivity


class DeveloperApplication : BaseApp() {
    // 全局的 handler 对象
    private val appHandler = Handler()
    // 全局的 Toast 对象
    private val flexibleToast: FlexibleToast by lazy {
        FlexibleToast(this)
    }
    private val builder: FlexibleToast.Builder by lazy {
        FlexibleToast.Builder(this).setGravity(FlexibleToast.GRAVITY_BOTTOM)
    }

    companion object {
        private lateinit var instance: DeveloperApplication
        fun getInstance(): DeveloperApplication {
            return instance
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
    override fun onCreate() {
        super.onCreate()
        BaseWidget.init(this)
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


    fun showToast(builder: FlexibleToast.Builder) {
        if (Looper.myLooper() !== Looper.getMainLooper()) {
            appHandler.post { flexibleToast.toastShow(builder) }
        } else {
            flexibleToast.toastShow(builder)
        }
    }

    override fun showToast(msg: String) {
        builder.setSecondText(msg)
        showToast(builder)
    }
}
