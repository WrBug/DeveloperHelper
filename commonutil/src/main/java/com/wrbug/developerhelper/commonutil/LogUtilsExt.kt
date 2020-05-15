package com.wrbug.developerhelper.commonutil

import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.internal.DefaultsFactory
import com.wrbug.developerhelper.commonutil.JsonHelper


private var init = false
fun Any.print() {
    if (!init) {
        XLog.init(
            LogConfiguration.Builder().logLevel(LogLevel.ALL).tag("developerHelper.print-->").build(),
            DefaultsFactory.createPrinter()
        )
        init = true
    }

    XLog.i("----------易开发Log----------")
    when (this) {
        is CharSequence, is Number, is Boolean -> XLog.i(this)
        is Throwable -> XLog.e(this)
        else -> XLog.json(JsonHelper.toJson(this))
    }
    XLog.i("----------结束----------")
}