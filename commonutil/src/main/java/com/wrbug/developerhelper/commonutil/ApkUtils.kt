package com.wrbug.developerhelper.commonutil

import android.content.Context
import android.content.pm.PackageManager
import com.wrbug.developerhelper.commonutil.entity.ApkSignInfo
import java.security.MessageDigest


object ApkUtils {

    fun getApkSignInfo(context: Context, packageName: String): ApkSignInfo {
        val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        val cert = packageInfo.signatures[0].toByteArray()
        val apkSignInfo = ApkSignInfo()
        apkSignInfo.sha1 = byte2String(
            getSha1(cert)
        )
        apkSignInfo.md5 = byte2String(
            getMd5(cert)
        )
        return apkSignInfo
    }


    private fun getMd5(cert: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("MD5")
        md.update(cert)
        return md.digest()
    }

    private fun getSha1(cert: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA1")
        md.update(cert)
        return md.digest()
    }

    private fun byte2String(data: ByteArray): String {
        val hexString = StringBuilder()
        for (byte in data) {
            val str = Integer.toHexString(0xFF and byte.toInt()).toUpperCase()
            if (str.length == 1) {
                hexString.append("0")
            }
            hexString.append(str).append(":")
        }
        return hexString.substring(0, hexString.length - 1).toString()
    }
}