package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import io.reactivex.rxjava3.core.Observable


/**
 *
 *  class: DumpDexListProcessDataManager.kt
 *  author: wrbug
 *  date: 2020-05-15
 *  description：
 *
 */
class DumpDexListProcessDataManager private constructor() :
    ProcessDataManager<DumpDexListProcessData>() {
    fun setData(list: List<String>) {
        processData?.setData(list)
    }

    fun getDataAsync(): Observable<List<String>> {
        if (processData == null) {
            return Observable.just(emptyList())
        }
        return processData!!.getData().map {
            it.fromJson<List<String>>() ?: emptyList()
        }.onErrorResumeNext {
            Observable.just(emptyList())
        }
    }

    fun getData(): ArrayList<String> {
        return ArrayList(getDataAsync().blockingFirst())
    }

    fun containPackage(packageName: String): Boolean {
        return getData().contains(packageName)
    }

    companion object {
        val instance = DumpDexListProcessDataManager()
    }
}