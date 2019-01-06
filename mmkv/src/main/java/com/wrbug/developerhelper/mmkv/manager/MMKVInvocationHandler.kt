package com.wrbug.developerhelper.mmkv.manager

import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class MMKVInvocationHandler(clazz: Class<*>) : InvocationHandler {
    val mmkv: MMKV = MMKV.mmkvWithID(clazz.name)
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        method?.let {
            if (it.name.startsWith("set") && args != null && args.size == 1) {
                setValue(it.name, args[0])
            } else if (it.name.startsWith("get")) {
                return getValue(it, 3)
            } else if (it.name.startsWith("is")) {
                return getValue(it, 2)
            }
        }
        return null
    }

    private fun getValue(method: Method, prefixLen: Int): Any? = with(method) {
        val key = name.substring(prefixLen)
        return when (returnType) {
            Boolean::class.java -> mmkv.decodeBool(key)
            Int::class.java -> mmkv.decodeInt(key)
            Long::class.java -> mmkv.decodeLong(key)
            Float::class.java -> mmkv.decodeFloat(key)
            Double::class.java -> mmkv.decodeDouble(key)
            String::class.java -> mmkv.decodeString(key)
            ByteArray::class.java -> mmkv.decodeBytes(key)
            else -> Gson().fromJson(mmkv.decodeString(key), returnType)
        }
    }

    private fun setValue(name: String, data: Any) {
        val key = name.substring(3)
        when (data) {
            is Boolean -> mmkv.encode(key, data)
            is Int -> mmkv.encode(key, data)
            is Long -> mmkv.encode(key, data)
            is Float -> mmkv.encode(key, data)
            is Double -> mmkv.encode(key, data)
            is String -> mmkv.encode(key, data)
            is ByteArray -> mmkv.encode(key, data)
            else -> mmkv.encode(key, Gson().toJson(data))
        }
    }

}