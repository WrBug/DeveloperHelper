package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.ipc.processshare.AppXposedProcessData

class AppXposedProcessDataManager private constructor() :
    ProcessDataManager<AppXposedProcessData>(),
    AppXposedProcessData {
    override fun setAppXposedStatusList(list: Map<String, Boolean>) {
        processData?.setAppXposedStatusList(list)
    }

    override fun getAppXposedStatusList() = processData?.getAppXposedStatusList()

    fun isAppXposedOpened(packageName: String): Boolean {
        val map = getAppXposedStatusList() ?: return false
        return map[packageName] ?: false
    }

    fun getOpenedAppXposedList(): List<String> {
        val map = getAppXposedStatusList() ?: return emptyList()
        val list = ArrayList<String>()
        map.forEach {
            if (it.value) {
                list.add(it.key)
            }
        }
        return list
    }

    fun setAppXposedStatus(packageName: String, open: Boolean) {
        val map = getAppXposedStatusList() ?: HashMap()
        map[packageName] = open
        setAppXposedStatusList(map)
    }

    companion object {
         val instance = AppXposedProcessDataManager()


    }
}