package com.wrbug.developerhelper.model.mmkv

interface ConfigKv {
    fun setOpenRoot(openRoot: Boolean)
    fun getOpenRoot(): Boolean
    fun setOpenXposed(openXposed: Boolean)
    fun getOpenXposed(): Boolean
}