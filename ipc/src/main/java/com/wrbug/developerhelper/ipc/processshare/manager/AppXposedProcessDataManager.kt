package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.ipc.processshare.AppXposedProcessData
import io.reactivex.rxjava3.core.Observable

class AppXposedProcessDataManager private constructor() :
    ProcessDataManager<AppXposedProcessData>() {
    fun setAppXposedStatusList(list: Map<String, Boolean>) {
        processData.setAppXposedStatusList(list)
    }

    fun setAppXposedStatusList(vararg pairs: Pair<String, Boolean>) {
        setAppXposedStatusList(mapOf(*pairs))
    }

    fun getAppXposedStatusListAsync(): Observable<Map<String, Boolean>> {
        return processData.getAppXposedStatusList().map {
            it.fromJson<Map<String, Boolean>>() ?: emptyMap()
        }.onErrorResumeNext {
            Observable.just(emptyMap())
        }
    }

    fun getAppXposedStatusList(): Map<String, Boolean> {
        return getAppXposedStatusListAsync().blockingFirst()
    }

    fun isAppXposedOpened(packageName: String): Boolean {
        return getAppXposedStatusList()[packageName] == true
    }

    fun getOpenedAppXposedList(): List<String> {
        val map = getAppXposedStatusList()
        val list = ArrayList<String>()
        map.forEach { entry ->
            if (entry.value) {
                list.add(entry.key)
            }
        }
        return list
    }

    fun setAppXposedStatus(packageName: String, open: Boolean) {
        getAppXposedStatusListAsync().subscribe({
            val map = HashMap(it)
            map[packageName] = open
            setAppXposedStatusList(map)
        }, {

        })

    }

    companion object {
        val instance = AppXposedProcessDataManager()


    }
}