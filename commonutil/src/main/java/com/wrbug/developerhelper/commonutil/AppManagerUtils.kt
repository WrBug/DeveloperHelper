package com.wrbug.developerhelper.commonutil

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.wrbug.developerhelper.commonutil.shell.ShellManager


object AppManagerUtils {
    fun uninstallApp(context: Context, packageName: String) {
        val uri = Uri.parse("package:$packageName")
        val intent = Intent(Intent.ACTION_DELETE, uri)
        context.startActivity(intent)
    }

    fun clearAppData(packageName: String): Boolean {
        return ShellManager.clearAppData(packageName)
    }

    fun forceStopApp(packageName: String): Boolean {
        return ShellManager.forceStopApp(packageName)
    }
}