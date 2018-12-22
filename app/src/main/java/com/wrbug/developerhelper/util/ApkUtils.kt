package com.wrbug.developerhelper.util

import android.content.Context
import android.content.pm.PackageManager
import java.security.MessageDigest

object ApkUtils {
    fun getSha1(context: Context, packageName: String): String {
        val packageInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        val cert = packageInfo.signingInfo.signingCertificateHistory[0].toByteArray()
        val digest = MessageDigest.getInstance("SHA1")
        val publicKey = digest.digest(cert)
        val hexString = StringBuilder()
        for (byte in publicKey) {
            val str = Integer.toHexString(0xFF and byte.toInt()).toUpperCase()
            if (str.length == 1) {
                hexString.append("0")
            }
            hexString.append(str).append(":")
        }
        return hexString.substring(0, hexString.length - 1).toString()
    }
}