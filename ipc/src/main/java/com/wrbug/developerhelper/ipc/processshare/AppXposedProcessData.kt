package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import io.reactivex.rxjava3.core.Observable

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
    fun setAppXposedStatus(map: Map<String, Boolean>)

    fun setAppXposedStatus(packageName: String, open: Boolean)
    fun getAppXposedStatus(): Observable<Map<String, Boolean>>
}