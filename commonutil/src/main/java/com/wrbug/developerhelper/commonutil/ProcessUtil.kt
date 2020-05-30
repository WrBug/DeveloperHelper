package com.wrbug.developerhelper.commonutil

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.text.TextUtils

import java.io.FileInputStream
import java.io.IOException

object ProcessUtil {

    fun isMainProc(context: Context): Boolean {
        val myPid = Process.myPid()
        var procName = readProcName(context, myPid)
        if (TextUtils.isEmpty(procName)) {
            procName = readProcName(myPid)
        }
        return context.packageName == procName
    }


    fun readProcName(context: Context, myPid: Int): String? {
        var myProcess: ActivityManager.RunningAppProcessInfo? = null
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        try {
            val list = activityManager.runningAppProcesses
            if (list != null) {
                for (info in list) {
                    if (info.pid == myPid) {
                        myProcess = info
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (myProcess != null) {
            return myProcess.processName
        }

        return null
    }

    fun readProcName(myPid: Int=Process.myPid()): String? {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream("/proc/$myPid/cmdline")
            val buffer = ByteArray(128)
            val len = fileInputStream.read(buffer)
            if (len <= 0) {
                return null
            }
            var index = 0
            while (index < buffer.size) {
                if (buffer[index] > 128 || buffer[index] <= 0) {
                    break
                }
                index++
            }
            return String(buffer, 0, index)
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        } finally {
            try {
                fileInputStream?.close()
            } catch (ignore: IOException) {
            }

        }
        return null
    }
}
