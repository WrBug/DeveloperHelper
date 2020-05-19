package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import io.reactivex.rxjava3.core.Observable

/**
 *
 *  class: FileProcessData.kt
 *  author: wrbug
 *  date: 2020-05-19
 *  description：
 *
 */
@Keep
interface FileProcessData : ProcessData {

    @Url(TcpUrl.FileProcessDataUrl.GET_DATA_FINDER_ZIP_FILE)
    fun getDataFinderZipFile(): Observable<String>
}