package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep

/**
 *
 *  class: AppXposedProcessData.kt
 *  author: wrbug
 *  date: 2020-05-14
 *  description：
 *
 */
@Keep
interface AppXposedProcessData : ProcessData {
    fun setAppXposedStatusList(list: Map<String, Boolean>)
    fun getAppXposedStatusList(): HashMap<String, Boolean>?
}