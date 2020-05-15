package com.wrbug.developerhelper.ipcserver

import android.content.Context
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.ipc.processshare.TcpUrl
import com.wrbug.developerhelper.ipcserver.annotation.Controller


/**
 *
 *  class: GlobalConfigProcessDataImpl.kt
 *  author: wrbug
 *  date: 2020-05-15
 *  description：
 *
 */
class GlobalConfigProcessDataImpl {

    companion object {
        private const val KEY_IS_XPOSED_OPEN = "isXposedOpen"
    }

    private val sp =
        BaseApp.instance.getSharedPreferences("ipc_global_config", Context.MODE_PRIVATE)

    @Controller(TcpUrl.GlobalConfigProcessDataUrl.IS_XPOSED_OPEN)
    fun isXposedOpen(): String {
        return sp.getBoolean(KEY_IS_XPOSED_OPEN, false).toString()
    }

    @Controller(TcpUrl.GlobalConfigProcessDataUrl.SET_XPOSED_OPEN)
    fun setXposedOpen(open: String) {
        sp.edit().putBoolean(KEY_IS_XPOSED_OPEN, open.toBoolean()).apply()
    }
}