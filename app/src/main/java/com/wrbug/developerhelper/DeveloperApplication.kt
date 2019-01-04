package com.wrbug.developerhelper

import android.os.Handler
import android.os.Looper
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.tencent.mmkv.MMKV
import com.wrbug.developerhelper.basecommon.BaseApp
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread
import com.wrbug.developerhelper.ui.widget.flexibletoast.FlexibleToast


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

    override fun onCreate() {
        super.onCreate()
        instance = this
        XLog.init(LogLevel.ALL)
        MMKV.initialize(this)
        releaseAssetsFile()
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
