@file:Suppress("UNCHECKED_CAST")

package com.wrbug.developerhelper.ipc.processshare

import com.wrbug.developerhelper.ipc.processshare.data.IpcFileDataManager
import java.lang.reflect.Proxy

object ProcessDataCreator {
    private val map = hashMapOf<Class<*>, Any>()
    fun <T : ProcessData> get(clazz: Class<T>): T {
        if (map.containsKey(clazz)) {
            return map[clazz] as T
        }
        val instance = obtainImpl(clazz)
        map[clazz] = instance
        return instance
    }

    private fun <T : ProcessData> obtainImpl(clazz: Class<T>): T {
        val service = IpcFileDataManager.getService(clazz)
        if (service != null) {
            return service
        }
        val instance = Proxy.newProxyInstance(
            clazz.classLoader, arrayOf(clazz),
            ProcessDataInvocationHandler()
        )
        return instance as T
    }
}
