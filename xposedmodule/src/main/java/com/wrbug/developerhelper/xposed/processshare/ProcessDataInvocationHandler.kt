package com.wrbug.developerhelper.xposed.processshare

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.commonutil.Base64
import com.wrbug.developerhelper.xposed.util.FileUtils
import com.wrbug.developerhelper.xposed.xposedLog
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
        "开始获取${clazz.name}.json".xposedLog()
        val data: Any = getDataFromFile()[key] ?: return null
        "获取成功: $data".xposedLog()
        return when (method.returnType) {
            Boolean::class.java,
            Int::class.java,
            Long::class.java,
            String::class.java,
            Float::class.java,
            Double::class.java -> data
            else -> gson.fromJson(data.toString(), method.returnType)
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
        "保存数据 $map".xposedLog()
        val file = File("/data/local/tmp/developerHelper", "${clazz.name}.txt")
        val json = Gson().toJson(map)
        json.xposedLog()
        val data = Base64.encodeAsString(json.toByteArray())
        data.xposedLog()
        val commandResult =
            Shell.SU.run("echo $data > ${file.absolutePath}  && chmod 777 ${file.absolutePath}")
        if (commandResult.isSuccessful) {
            "${clazz.name}.json 保存成功".xposedLog()
        } else {
            "${clazz.name}.json 保存失败：${commandResult.getStderr()}".xposedLog()

        }
    }

    private fun getDataFromFile(): HashMap<String, Any?> {
        try {
            val file = File("/data/local/tmp/developerHelper", "${clazz.name}.txt")
            val json = Base64.decode(FileUtils.readFile(file).replace("\n", ""))
            return Gson().fromJson(json, object : TypeToken<HashMap<String, Any?>>() {}.type)
                ?: return hashMapOf()
        } catch (t: Throwable) {
            t.xposedLog()
        }
        return hashMapOf()
    }
}