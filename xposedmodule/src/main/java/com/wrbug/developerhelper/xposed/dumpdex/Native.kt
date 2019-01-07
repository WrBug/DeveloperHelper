package com.wrbug.developerhelper.xposed.dumpdex

import com.wrbug.developerhelper.xposed.xposedLog

/**
 * Native
 *
 * @author WrBug
 * @since 2018/3/23
 */
object Native {

    const val SO_FILE = "nativeDump.so"
    const val SO_FILE_V7a = "nativeDumpV7a.so"
    const val SO_FILE_V8a = "nativeDumpV8a.so"

    init {
        if (loadLib(SO_FILE_V7a) || loadLib(SO_FILE) || loadLib(SO_FILE_V8a)) {
            "动态库加载成功".xposedLog()
        }

    }

    private fun loadLib(file: String) = try {
        System.load("/data/local/tmp/$file")
        "loaded $file".xposedLog()
        true
    } catch (t: Throwable) {
        "load $file failed".xposedLog()
        t.xposedLog()
        false
    }

    external fun dump(packageName: String)
}
