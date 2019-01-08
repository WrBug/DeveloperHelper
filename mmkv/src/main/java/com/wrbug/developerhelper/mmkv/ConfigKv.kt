package com.wrbug.developerhelper.mmkv

interface ConfigKv {
    fun setOpenRoot(openRoot: Boolean)
    fun isOpenRoot(): Boolean
}