package com.wrbug.developerhelper.ipc.processshare

interface GlobalConfigProcessData : ProcessData {
    @DefaultValue("true")
    fun isXposedOpen(): Boolean

    fun setXposedOpen(open: Boolean)
}
