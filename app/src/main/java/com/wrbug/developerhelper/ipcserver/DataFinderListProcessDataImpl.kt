package com.wrbug.developerhelper.ipcserver

import android.content.Context
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.ipc.processshare.TcpUrl
import com.wrbug.developerhelper.ipcserver.annotation.Controller


/**
 *
 *  class: DataFinderListProcessDataImpl.kt
 *  author: wrbug
 *  date: 2020-05-19
 *  description：
 *
 */
class DataFinderListProcessDataImpl {


    private val sp =
        BaseApp.instance.getSharedPreferences("ipc_data_finder_list_config", Context.MODE_PRIVATE)

    @Controller(TcpUrl.DataFinderListProcessDataUrl.SET_DATA)
    fun setData(data: String) {
        val map = data.fromJson<Map<String, Boolean>>() ?: return
        val editor = sp.edit()
        map.forEach {
            editor.putBoolean(it.key, it.value)
        }
        editor.apply()
    }

    @Controller(TcpUrl.DataFinderListProcessDataUrl.GET_DATA)
    fun getData(): String {
        return sp.all.toJson() ?: "{}"
    }
}