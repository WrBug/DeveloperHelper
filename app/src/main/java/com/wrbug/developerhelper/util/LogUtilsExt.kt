package com.wrbug.developerhelper.util

import com.elvishew.xlog.XLog


fun Any.print() {
    XLog.i("----------易开发Log----------")
    when (this) {
        is CharSequence, is Number, is Boolean -> XLog.i(this)
        else -> XLog.json(JsonHelper.toJson(this))
    }
    XLog.i("----------结束----------")
}