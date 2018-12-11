package com.wrbug.developerhelper.model.mmkv.manager

import android.support.v4.util.ArrayMap
import java.lang.reflect.Proxy

object MMKVManager {
    val map = ArrayMap<Class<*>, Any>()
    fun <T> get(clazz: Class<T>): T {
        if (map.containsKey(clazz)) {
            return map[clazz] as T
        }
        val instance = obtainImpl(clazz)
        map[clazz] = instance
        return instance as T
    }

    private fun <T> obtainImpl(clazz: Class<T>): T? {
        val instance = Proxy.newProxyInstance(clazz.classLoader, arrayOf(clazz), MMKVInvocationHandler(clazz))
        return instance as T?
    }
}
