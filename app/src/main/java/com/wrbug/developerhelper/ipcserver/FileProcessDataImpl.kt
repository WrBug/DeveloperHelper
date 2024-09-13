package com.wrbug.developerhelper.ipcserver

import com.wrbug.developerhelper.base.BaseApp
import com.wrbug.developerhelper.commonutil.Base64
import com.wrbug.developerhelper.ipc.processshare.TcpUrl
import com.wrbug.developerhelper.ipcserver.annotation.Controller


/**
 *
 *  class: FileProcessDataImpl.kt
 *  author: wrbug
 *  date: 2020-05-19
 *  description：
 *
 */
class FileProcessDataImpl {
    @Controller(TcpUrl.FileProcessDataUrl.GET_DATA_FINDER_ZIP_FILE)
    fun getDataFinderZipFile(): String {
        val data = BaseApp.instance.assets.open("datafinder-web/web-static.zip").readBytes()
        val base64 = Base64.encodeAsString(data)
        return base64
    }

    @Controller(TcpUrl.FileProcessDataUrl.GET_DUMP_SO_ZIP_FILE)
    fun getDumpSoZipFile(): String {
        val data = BaseApp.instance.assets.open("nativeDump.zip").readBytes()
        val base64 = Base64.encodeAsString(data)
        return base64
    }
}