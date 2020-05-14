package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.ipc.processshare.AppXposedProcessData

class AppXposedProcessDataManager private constructor() :
    ProcessDataManager<AppXposedProcessData>(),
    AppXposedProcessData {
    override fun setAppXposedStatusList(list: Map<String, Boolean>) {
        processData?.setAppXposedStatusList(list)
    }

    override fun getAppXposedStatusList() = processData?.getAppXposedStatusList()

    companion object {
        private val instance = AppXposedProcessDataManager()
        @JvmStatic
        fun setAppXposedStatus(packageName: String, open: Boolean) {
            val map = instance.getAppXposedStatusList() ?: HashMap()
            map[packageName] = open
            instance.setAppXposedStatusList(map)
        }

        @JvmStatic
        fun isAppXposedOpened(packageName: String): Boolean {
            val map = instance.getAppXposedStatusList() ?: return false
            return map[packageName] ?: false
        }

        @JvmStatic
        fun getOpenedAppXposedList(): List<String> {
            val map = instance.getAppXposedStatusList() ?: return emptyList()
            val list = ArrayList<String>()
            map.forEach {
                if (it.value) {
                    list.add(it.key)
                }
            }
            return list
        }

    }
}