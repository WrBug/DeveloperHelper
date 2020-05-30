package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.ipc.processshare.AppXposedProcessData

class AppXposedProcessDataManager private constructor() :
    ProcessDataManager<AppXposedProcessData>() {
    fun setAppXposedStatusList(list: Map<String, Boolean>) {
        processData.setAppXposedStatus(list)
    }

    fun setAppXposedStatusList(vararg pairs: Pair<String, Boolean>) {
        setAppXposedStatusList(mapOf(*pairs))
    }


    fun getAppXposedStatusList(): Map<String, Boolean> {
        return processData.getAppXposedStatus().blockingFirst()
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
        processData.setAppXposedStatus(packageName, open)

    }

    companion object {
        val instance = AppXposedProcessDataManager()


    }
}