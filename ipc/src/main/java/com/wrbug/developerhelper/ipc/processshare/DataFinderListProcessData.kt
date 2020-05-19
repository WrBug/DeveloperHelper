package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import io.reactivex.rxjava3.core.Observable

@Keep
interface DataFinderListProcessData : ProcessData {

    @Url(TcpUrl.DataFinderListProcessDataUrl.SET_DATA)
    fun setData(map: Map<String, Boolean>)

    @Url(TcpUrl.DataFinderListProcessDataUrl.GET_DATA)
    fun getData(): Observable<String>

}