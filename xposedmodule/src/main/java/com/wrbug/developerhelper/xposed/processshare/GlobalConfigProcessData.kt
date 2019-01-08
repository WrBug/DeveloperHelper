package com.wrbug.developerhelper.xposed.processshare

interface GlobalConfigProcessData : ProcessData {
    @DefaultValue("true")
    fun isXposedOpen(): Boolean

    fun setXposedOpen(open: Boolean)
}
