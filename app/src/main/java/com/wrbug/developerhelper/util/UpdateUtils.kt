package com.wrbug.developerhelper.util

import com.wrbug.developerhelper.commonutil.HttpUtil
import com.wrbug.developerhelper.commonutil.shell.Callback
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import com.wrbug.developerhelper.commonutil.OkhttpUtils
import com.wrbug.developerhelper.model.entity.VersionInfo
import org.jsoup.Jsoup
import java.lang.Exception


object UpdateUtils {
    private const val URL = "https://www.coolapk.com/apk/com.wrbug.developerhelper"
    fun checkUpdate(callback: Callback<VersionInfo>) {
        doAsync {
            try {
                val document = Jsoup.connect(URL)
                    .sslSocketFactory(OkhttpUtils.createSSLSocketFactory()).get()
                val versionName = document.getElementsByClass("list_app_info").text() ?: ""
                val feature = document.getElementsByClass("apk_left_title_info").first().html().replace("<br>", "\n")
                val size = document.getElementsByClass("apk_topba_message").html().split("/")[0].trim()
                val updateTime =
                    document.getElementsByClass("apk_left_title_info")[2].html().split("<br>")[1].replace(
                        "更新时间：",
                        ""
                    )
                val info = VersionInfo()
                info.versionName = versionName
                info.feature = feature
                info.size = size
                info.updateDate = updateTime
                info.downloadUrl = URL
                uiThread {
                    callback.onSuccess(info)
                }
            } catch (e: Exception) {
                uiThread { callback.onFailed() }
            }

        }

    }
}