package com.wrbug.developerhelper.commonutil

import com.elvishew.xlog.XLog
import com.wrbug.developerhelper.commonutil.JsonHelper


fun Any.print() {
    XLog.i("----------易开发Log----------")
    when (this) {
        is CharSequence, is Number, is Boolean -> XLog.i(this)
        else -> XLog.json(JsonHelper.toJson(this))
    }
    XLog.i("----------结束----------")
}