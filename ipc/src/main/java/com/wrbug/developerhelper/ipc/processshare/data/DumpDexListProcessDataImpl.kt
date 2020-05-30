package com.wrbug.developerhelper.ipc.processshare.data

import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *
 *  class: DumpDexListProcessDataImpl.kt
 *  author: wrbug
 *  date: 2020-05-15
 *  description：
 *
 */
class DumpDexListProcessDataImpl : DumpDexListProcessData {
    private val file = IpcFileDataManager.getFile("DataFinderListProcessData")
    private fun getMap(): Map<String, Boolean> {
        return try {
            file.getData().mapValues { it.value as Boolean }
        } catch (t: Throwable) {
            emptyMap()
        }
    }

    override fun setData(map: Map<String, Boolean>) {
        val m = HashMap(getMap())
        m.putAll(map)
        file.save(m)
    }

    override fun getData(): Observable<Map<String, Boolean>> {
        return Observable.just(getMap())
            .subscribeOn(Schedulers.io())
            .onErrorReturn {
                emptyMap()
            }
    }


}