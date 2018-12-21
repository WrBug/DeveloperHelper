package com.wrbug.developerhelper

import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.tencent.mmkv.MMKV
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.util.ShellUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread


class DeveloperApplication : BaseApp() {

    override fun onCreate() {
        super.onCreate()
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
}
