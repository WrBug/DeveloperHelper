package com.wrbug.developerhelper.shell

import android.os.Build
import com.jaredrummler.android.shell.CommandResult
import com.wrbug.developerhelper.util.ShellUtils
import java.util.regex.Pattern

object ShellManager {
    private const val SHELL_TOP_ACTIVITY_LOW = "dumpsys activity | grep mFocusedActivity"
    private const val SHELL_TOP_ACTIVITY_NEW = "dumpsys activity | grep mResumedActivity"
    private const val SHELL_PROCESS_PID_1 = "ps -ef | grep \"%s\" | grep -v grep | awk '{print \$2}'"
    private const val SHELL_PROCESS_PID_2 = "top -b -n 1 |grep %s |grep -v grep"
    private const val SHELL_PROCESS_PID_3 = "top -n 1 |grep %s |grep -v grep"
    private var SHELL_OPEN_ACCESSiBILITY_SERVICE = arrayOf(
        "settings put secure enabled_accessibility_services com.wrbug.developerhelper/com.wrbug.developerhelper.service.DeveloperHelperAccessibilityService",
        "settings put secure accessibility_enabled 1"
    )

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

    fun getPid(packageName: String): String {
        var result: CommandResult = ShellUtils.runWithSu(String.format(SHELL_PROCESS_PID_1, packageName))
        if (result.isSuccessful) {
            return result.getStdout()
        }
        result = ShellUtils.runWithSu(String.format(SHELL_PROCESS_PID_2, packageName))
        if (result.isSuccessful.not()) {
            result = ShellUtils.runWithSu(String.format(SHELL_PROCESS_PID_3, packageName))
        }
        if (result.isSuccessful.not()) {
            return ""
        }
        return result.getStdout().trim().split(" ")[0]
    }

    fun openAccessibilityService(): Boolean {
        val commandResult = ShellUtils.runWithSu(*SHELL_OPEN_ACCESSiBILITY_SERVICE)
        return commandResult.isSuccessful && commandResult.getStdout().isEmpty()
    }
}
