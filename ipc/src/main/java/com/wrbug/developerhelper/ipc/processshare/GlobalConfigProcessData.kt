package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import io.reactivex.rxjava3.core.Observable

@Keep
interface GlobalConfigProcessData : ProcessData {
    @Url(TcpUrl.GlobalConfigProcessDataUrl.IS_XPOSED_OPEN)
    fun isXposedOpen(): Observable<String>

    @Url(TcpUrl.GlobalConfigProcessDataUrl.SET_XPOSED_OPEN)
    fun setXposedOpen(open: Boolean)
}
