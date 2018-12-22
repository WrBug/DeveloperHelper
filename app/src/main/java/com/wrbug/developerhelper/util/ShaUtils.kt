package com.wrbug.developerhelper.util

import android.provider.SyncStateContract.Helpers.update
import java.security.MessageDigest
import kotlin.experimental.and


object ShaUtils {
    fun getSha1(data: ByteArray): String {
        if (data.isEmpty()) {
            return ""
        }
        val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        try {
            val mdTemp = MessageDigest.getInstance("SHA1")
            mdTemp.update(data)

            val md = mdTemp.digest()
            val j = md.size
            val buf = CharArray(j * 2)
            var k = 0
            for (i in 0 until j) {
                val byte0 = md[i]
                buf[k++] = hexDigits[byte0.toInt().ushr(4) and 0xf]
                buf[k++] = hexDigits[byte0.toInt() and 0xf]
            }
            return String(buf)
        } catch (e: Exception) {
            return ""
        }

    }
}