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
    fun setData(map: Map<String, Boolean>) {
        processData.setData(map)
    }

    fun setData(vararg pairs: Pair<String, Boolean>) {
        processData.setData(hashMapOf(*pairs))
    }

    fun getData(): HashMap<String, Boolean> {
        return HashMap(processData.getData().blockingFirst())
    }

    fun containPackage(packageName: String): Boolean {
        return getData()[packageName] ?: false
    }

    companion object {
        val instance = DumpDexListProcessDataManager()
    }
}