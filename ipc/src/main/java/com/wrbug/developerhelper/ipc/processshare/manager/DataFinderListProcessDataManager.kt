package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.ipc.processshare.DataFinderListProcessData
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import io.reactivex.rxjava3.core.Observable


/**
 *
 *  class: DataFinderListProcessDataManager.kt
 *  author: wrbug
 *  date: 2020-05-19
 *  description：
 *
 */
class DataFinderListProcessDataManager private constructor() :
    ProcessDataManager<DataFinderListProcessData>() {
    fun setData(map: Map<String, Boolean>) {
        processData?.setData(map)
    }

    fun setData(vararg pairs: Pair<String, Boolean>) {
        processData?.setData(hashMapOf(*pairs))
    }

    private fun getDataAsync(): Observable<Map<String, Boolean>> {
        if (processData == null) {
            return Observable.just(emptyMap())
        }
        return processData!!.getData().map {
            it.fromJson<Map<String, Boolean>>() ?: emptyMap()
        }.onErrorResumeNext {
            Observable.just(emptyMap())
        }
    }

    fun getData(): HashMap<String, Boolean> {
        return HashMap(getDataAsync().blockingFirst())
    }

    fun containPackage(packageName: String): Boolean {
        return getData()[packageName] ?: false
    }

    companion object {
        val instance = DataFinderListProcessDataManager()
    }
}