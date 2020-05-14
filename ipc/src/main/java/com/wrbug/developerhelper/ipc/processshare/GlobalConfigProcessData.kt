package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep

@Keep
interface GlobalConfigProcessData : ProcessData {
    @DefaultValue("true")
    fun isXposedOpen(): Boolean

    fun setXposedOpen(open: Boolean)
}
