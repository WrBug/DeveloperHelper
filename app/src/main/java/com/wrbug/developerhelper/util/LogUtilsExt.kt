package com.wrbug.developerhelper.util

import com.elvishew.xlog.XLog


fun Any.print() {
    when (this) {
        is CharSequence, is Number -> XLog.i(this)
        else -> XLog.json(JsonHelper.toJson(this))
    }
}