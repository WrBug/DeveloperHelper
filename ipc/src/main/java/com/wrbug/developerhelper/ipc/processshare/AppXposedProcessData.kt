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
    @Url(TcpUrl.AppXposedProcessDataUrl.SET_APP_XPOSED_STATUS_LIST)
    fun setAppXposedStatusList(list: Map<String, Boolean>)

    @Url(TcpUrl.AppXposedProcessDataUrl.GET_APP_XPOSED_STATUS_LIST)
    fun getAppXposedStatusList(): Observable<String>
}