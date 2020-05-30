package com.wrbug.developerhelper.ipc.processshare.data

import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.ipc.processshare.AppXposedProcessData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

class AppXposedProcessDataImpl : AppXposedProcessData {
    private val file = IpcFileDataManager.getFile("AppXposedProcessData")
    private fun getMap(): Map<String, Boolean> {
        return try {
            file.getData().mapValues { it.value as Boolean }
        } catch (t: Throwable) {
            emptyMap()
        }
    }

    override fun setAppXposedStatus(map: Map<String, Boolean>) {
        val m = HashMap(getMap())
        m.putAll(map)
        file.save(m)
    }

    override fun setAppXposedStatus(packageName: String, open: Boolean) {
        setAppXposedStatus(mapOf(packageName to open))
    }

    override fun getAppXposedStatus(): Observable<Map<String, Boolean>> {
        return Observable.just(getMap())
            .subscribeOn(Schedulers.io())
            .onErrorReturn {
                emptyMap()
            }
    }

}