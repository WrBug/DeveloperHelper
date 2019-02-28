package com.wrbug.developerhelper.ipc.processshare

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.commonutil.Base64
import com.wrbug.developerhelper.commonutil.FileUtils
import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ProcessDataInvocationHandler(val clazz: Class<*>) : InvocationHandler {
    companion object {
        private val gson = Gson()
    }

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
        val data: Any = getDataFromFile()[key] ?: return defaultValue(method)
        val value = when (method.returnType) {
            Boolean::class.java,
            Int::class.java,
            Long::class.java,
            String::class.java,
            Float::class.java,
            Double::class.java -> data
            else -> gson.fromJson(data.toString(), method.returnType)
        }
        if (value != null) {
            return value
        }
        return defaultValue(method)
    }

    private fun defaultValue(method: Method): Any? {
        val annotation = method.getAnnotation(DefaultValue::class.java) ?: return null
        if (method.returnType == String::class.java) {
            return annotation.value
        }
        return when (method.returnType) {
            Boolean::class.java -> annotation.value.toBoolean()
            Int::class.java -> annotation.value.toInt()
            Long::class.java -> annotation.value.toLong()
            Float::class.java -> annotation.value.toFloat()
            Double::class.java -> annotation.value.toDouble()
            else -> null
        }

    }

    private fun setValue(name: String, data: Any) {
        val key = name.substring(3)
        val map = getDataFromFile()
        when (data) {
            is Boolean, Int, Long, String, Float, Double -> map[key] = data
            else -> map[key] = gson.toJson(data)
        }
        saveData(map)
    }

    private fun saveData(map: HashMap<String, Any?>) {
        val file = File("/data/local/tmp/developerHelper", "${clazz.name}.txt")
        val json = Gson().toJson(map)
        val data = Base64.encodeAsString(json.toByteArray())
        val commandResult =
            Shell.SU.run("echo $data > ${file.absolutePath}  && chmod 777 ${file.absolutePath}")
    }

    private fun getDataFromFile(): HashMap<String, Any?> {
        try {
            val file = File("/data/local/tmp/developerHelper", "${clazz.name}.txt")
            val json = Base64.decode(FileUtils.readFile(file).replace("\n", ""))
            return Gson().fromJson(json, object : TypeToken<HashMap<String, Any?>>() {}.type)
                ?: return hashMapOf()
        } catch (t: Throwable) {
        }
        return hashMapOf()
    }
}