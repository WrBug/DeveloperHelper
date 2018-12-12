package com.wrbug.developerhelper

import android.app.Application
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.tencent.mmkv.MMKV
import com.wrbug.developerhelper.basecommon.BaseApp


class DeveloperApplication : BaseApp() {

    override fun onCreate() {
        super.onCreate()
        XLog.init(LogLevel.ALL)
        MMKV.initialize(this)
    }
}
