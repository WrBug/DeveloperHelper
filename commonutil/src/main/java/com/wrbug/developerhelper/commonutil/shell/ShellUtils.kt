package com.wrbug.developerhelper.commonutil.shell

import android.util.Log
import com.wrbug.developerhelper.commonutil.CommonUtils
import com.wrbug.developerhelper.commonutil.RootUtils
import io.reactivex.rxjava3.core.Single
import java.io.File

object ShellUtils {
    private const val TAG = "ShellUtils"
    private const val AVAILABLE_TEST_COMMANDS = "echo -BOC- \n id"
    val busyBoxFile by lazy {
        File(CommonUtils.application.cacheDir, "busybox")
    }

    fun run(cmd: String, useBusyBox: Boolean = true): CommandResult {
        return Shell.SH.run(appendBusyBox(useBusyBox, cmd)).convert()
    }

    fun runWithSuAsync(vararg cmds: String, useBusyBox: Boolean = true): Single<CommandResult> {
        return Single.just(cmds).map {
            if (!RootUtils.isRoot()) {
                throw ShellException("未开启root权限")
            }
            Shell.SU.run(appendBusyBox(useBusyBox, *cmds)).convert()
        }
    }

    fun runWithSu(vararg cmd: String, useBusyBox: Boolean = true): CommandResult {
        if (!RootUtils.isRoot()) {
            return CommandResult(
                emptyList(),
                listOf("未开启root权限"),
                -1, null
            )
        }
        return Shell.SU.run(appendBusyBox(useBusyBox, *cmd)).convert()
    }

    private fun Shell.Command.Result.convert(): CommandResult {
        return apply {
            if (isSuccess.not()) {
                Log.e(TAG, stderr.joinToString("\n"))
            }
        }.let {
            CommandResult(it.stdout, it.stderr, exitCode, it.details)
        }
    }

    private fun appendBusyBox(useBusyBox: Boolean, vararg cmds: String): String {
        if (cmds.isEmpty()) {
            return ""
        }
        if (useBusyBox && busyBoxFile.exists() && busyBoxFile.canExecute()) {
            return cmds.joinToString("\n") { busyBoxFile.absolutePath + " " + it }
        }
        return cmds.joinToString("\n")
    }

    fun isRoot(): Boolean {
        val result = Shell.SU.run(AVAILABLE_TEST_COMMANDS)
        return parseAvailableResult(result.stdout, true)
    }


    private fun parseAvailableResult(stdout: List<String>?, checkForRoot: Boolean): Boolean {
        if (stdout == null) {
            return false
        }
        // this is only one of many ways this can be done
        var echoSeen = false
        for (line in stdout) {
            if (line.contains("uid=")) {
                // id command is working, let's see if we are actually root
                return !checkForRoot || line.contains("uid=0")
            } else if (line.contains("-BOC-")) {
                // if we end up here, at least the su command starts some kind of shell, let's hope it has root privileges -
                // no way to know without additional native binaries
                echoSeen = true
            }
        }
        return echoSeen
    }
}