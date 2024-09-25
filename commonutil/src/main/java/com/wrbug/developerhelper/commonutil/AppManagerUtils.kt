package com.wrbug.developerhelper.commonutil

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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

    fun restartApp(context: Context, packageName: String) {
        if (!forceStopApp(packageName)) {
            Toast.makeText(context, "重启失败", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}