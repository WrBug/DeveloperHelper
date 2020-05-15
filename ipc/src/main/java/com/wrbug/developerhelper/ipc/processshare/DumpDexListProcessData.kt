package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import io.reactivex.rxjava3.core.Observable

@Keep
interface DumpDexListProcessData : ProcessData {

    @Url(TcpUrl.DumpDexListProcessDataUrl.SET_DATA)
    fun setData(list: List<String>)

    @Url(TcpUrl.DumpDexListProcessDataUrl.GET_DATA)
    fun getData(): Observable<String>
}