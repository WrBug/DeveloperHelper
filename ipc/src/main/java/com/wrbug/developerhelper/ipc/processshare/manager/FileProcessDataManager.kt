package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.commonutil.Base64
import com.wrbug.developerhelper.ipc.processshare.FileProcessData
import io.reactivex.rxjava3.core.Observable


/**
 *
 *  class: FileProcessDataManager.kt
 *  author: wrbug
 *  date: 2020-05-19
 *  description：
 *
 */
class FileProcessDataManager private constructor() :
    ProcessDataManager<FileProcessData>() {

    fun getDataFinderZipFile(): Observable<ByteArray> {
        return processData.getDataFinderZipFile().map {
            Base64.decode(it.toCharArray())
        }
    }

    fun getDumpSoZipFile(): Observable<ByteArray> {
        return processData.getDumpSoZipFile().map {
            Base64.decode(it.toCharArray())
        }
    }

    companion object {
        val instance = FileProcessDataManager()
    }
}