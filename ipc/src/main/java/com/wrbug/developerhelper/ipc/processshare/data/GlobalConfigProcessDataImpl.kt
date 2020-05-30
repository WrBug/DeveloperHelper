package com.wrbug.developerhelper.ipc.processshare.data

import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.ipc.processshare.GlobalConfigProcessData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 *
 *  class: GlobalConfigProcessDataImpl.kt
 *  author: wrbug
 *  date: 2020-05-15
 *  description：
 *
 */
class GlobalConfigProcessDataImpl : GlobalConfigProcessData {
    companion object {
        private const val KEY_IS_XPOSED_OPEN = "isXposedOpen"
    }

    private val file = IpcFileDataManager.getFile("DataFinderListProcessData")
    private fun getMap(): Map<String, Any> {
        return try {
            file.getData()
        } catch (t: Throwable) {
            emptyMap()
        }
    }

    private fun setValue(key: String, value: Any) {
        try {
            val map = HashMap(getMap())
            map[key] = value
            file.save(map)
        } catch (t: Throwable) {
            t.printStackTrace()
        }

    }

    override fun isXposedOpen(): Observable<Boolean> {
        return Observable.just(getMap()[KEY_IS_XPOSED_OPEN] as Boolean)
            .subscribeOn(Schedulers.io())
            .onErrorReturn {
                false
            }
    }

    override fun setXposedOpen(open: Boolean) {
        setValue(KEY_IS_XPOSED_OPEN, open)
    }

}