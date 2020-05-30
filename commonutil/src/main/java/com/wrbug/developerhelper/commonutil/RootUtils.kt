package com.wrbug.developerhelper.commonutil

import com.wrbug.developerhelper.mmkv.ConfigKv
import com.wrbug.developerhelper.mmkv.manager.MMKVManager

object RootUtils {
    val configKv = MMKVManager.get(ConfigKv::class.java)
    fun isRoot() = configKv.isOpenRoot().not()
}