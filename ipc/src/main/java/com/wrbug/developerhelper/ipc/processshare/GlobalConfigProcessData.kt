package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import io.reactivex.rxjava3.core.Observable

@Keep
interface GlobalConfigProcessData : ProcessData {
    fun isXposedOpen(): Observable<Boolean>

    fun setXposedOpen(open: Boolean)
}
