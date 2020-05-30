package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import io.reactivex.rxjava3.core.Observable

@Keep
interface DataFinderListProcessData : ProcessData {

    fun setData(map: Map<String, Boolean>)

    fun getData(): Observable<Map<String, Boolean>>

}