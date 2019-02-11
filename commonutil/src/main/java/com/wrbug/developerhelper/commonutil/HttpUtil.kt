package com.wrbug.developerhelper.commonutil

import okhttp3.*

import java.io.IOException
import java.net.URLEncoder
import java.util.HashMap

object HttpUtil {

    fun post(url: String, headers: Headers? = null, param: Param): Response? {
        val client = OkhttpUtils.client
        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(mediaType, param.toString())
        val builder = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("content-type", "application/x-www-form-urlencoded")
        if (headers != null) {
            builder.headers(headers)
        }
        try {
            return client.newCall(builder.build()).execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    operator fun get(url: String, headers: Headers? = null, param: Param): Response? {
        var url = url
        val client = OkhttpUtils.client
        url = url + "?" + param.toString()
        val builder = Request.Builder()
            .url(url)
            .get()
        if (headers != null) {
            builder.headers(headers)
        }
        try {
            return client.newCall(builder.build()).execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    class Param : HashMap<String, Any>() {

        override fun toString(): String {
            val builder = StringBuilder()
            if (containsKey("key")) {
                remove("key")
            }
            for ((key, value) in this) {
                builder.append(key).append("=").append(urlEncode(value)).append("&")
            }
            if (builder.isNotEmpty()) {
                builder.deleteCharAt(builder.length - 1)
            }
            return builder.toString()
        }

        fun urlEncode(str: Any?): String {
            return if (str == null) {
                ""
            } else URLEncoder.encode(str.toString())
        }
    }

}
