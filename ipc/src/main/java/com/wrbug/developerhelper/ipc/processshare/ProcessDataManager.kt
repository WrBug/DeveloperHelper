@file:Suppress("UNCHECKED_CAST")

package com.wrbug.developerhelper.ipc.processshare

import androidx.collection.ArrayMap
import java.lang.reflect.Proxy

object ProcessDataManager {
    private val map = ArrayMap<Class<*>, Any>()
    fun <T : ProcessData> get(clazz: Class<T>): T {
        if (map.containsKey(clazz)) {
            return map[clazz] as T
        }
        val instance = obtainImpl(clazz)
        map[clazz] = instance
        return instance
    }

    private fun <T : ProcessData> obtainImpl(clazz: Class<T>): T {
        val instance = Proxy.newProxyInstance(
            clazz.classLoader, arrayOf(clazz),
            ProcessDataInvocationHandler(clazz)
        )
        return instance as T
    }
}
