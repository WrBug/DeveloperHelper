package com.wrbug.developerhelper.shell

import android.os.Build
import com.jaredrummler.android.shell.CommandResult
import com.wrbug.developerhelper.util.ShellUtils
import java.util.regex.Pattern

object ShellManager {
    private const val SHELL_TOP_ACTIVITY_LOW = "dumpsys activity | grep mFocusedActivity"
    private const val SHELL_TOP_ACTIVITY_NEW = "dumpsys activity | grep mResumedActivity"
    fun getTopActivity(): String {
        val result: CommandResult = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            ShellUtils.runWithSu(SHELL_TOP_ACTIVITY_LOW)
        } else {
            ShellUtils.runWithSu(SHELL_TOP_ACTIVITY_NEW)
        }

        result.getStdout().takeIf {
            val regex = "([\\s\\S]*)\\{[0-9a-fA-F]+ ([\\s\\S]*) ([\\s\\S]*)/([\\s\\S]*) ([\\s\\S]*)\\}"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(it)
            matcher.find()
        }?.let {
            val regex = "[a-zA-Z0-9\\.]+/[a-zA-Z0-9\\.]+"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(it)
            if (matcher.find()) {
                val result = matcher.group()
                val split = result.split("/")
                return if (split[1].startsWith(".")) {
                    split[0] + split[1]
                } else {
                    split[1]
                }
            }
        }
        return ""
    }
}
