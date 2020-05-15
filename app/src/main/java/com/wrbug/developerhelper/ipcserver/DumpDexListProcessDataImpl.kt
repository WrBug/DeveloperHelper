package com.wrbug.developerhelper.ipcserver

import android.content.Context
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.ipc.processshare.TcpUrl
import com.wrbug.developerhelper.ipcserver.annotation.Controller


/**
 *
 *  class: DumpDexListProcessDataImpl.kt
 *  author: wrbug
 *  date: 2020-05-15
 *  description：
 *
 */
class DumpDexListProcessDataImpl {
    companion object {
        private const val KEY_LIST = "list"
    }

    private val sp =
        BaseApp.instance.getSharedPreferences("ipc_dump_dex_list_config", Context.MODE_PRIVATE)

    @Controller(TcpUrl.DumpDexListProcessDataUrl.SET_DATA)
    fun setData(data: String) {
        sp.edit().putString(KEY_LIST, data).apply()
    }

    @Controller(TcpUrl.DumpDexListProcessDataUrl.GET_DATA)
    fun getData(): String {
        return sp.getString(KEY_LIST, "[]") ?: "[]"
    }
}