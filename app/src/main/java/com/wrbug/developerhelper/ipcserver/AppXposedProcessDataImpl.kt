package com.wrbug.developerhelper.ipcserver

import android.content.Context
import com.wrbug.developerhelper.basecommon.BaseApp
import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.ipc.processshare.TcpUrl
import com.wrbug.developerhelper.ipcserver.annotation.Controller

class AppXposedProcessDataImpl {

    private val sp =
        BaseApp.instance.getSharedPreferences("ipc_app_xposed_config", Context.MODE_PRIVATE)

    @Controller(TcpUrl.AppXposedProcessDataUrl.SET_APP_XPOSED_STATUS_LIST)
    fun setAppXposedStatusList(data: String) {
        val map = data.fromJson<Map<String, Boolean>>() ?: return
        val editor = sp.edit()
        map.forEach {
            editor.putBoolean(it.key, it.value)
        }
        editor.apply()
    }

    @Controller(TcpUrl.AppXposedProcessDataUrl.GET_APP_XPOSED_STATUS_LIST)
    fun getAppXposedStatusList(): String {
        return sp.all.toJson() ?: "{}"
    }

}