package com.wrbug.developerhelper.mmkv

interface ConfigKv {
    fun setOpenRoot(openRoot: Boolean)
    fun isOpenRoot(): Boolean
    fun setOpenXposed(openXposed: Boolean)
    fun isOpenXposed(): Boolean
}