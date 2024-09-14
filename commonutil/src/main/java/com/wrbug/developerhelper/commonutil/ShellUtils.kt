package com.wrbug.developerhelper.commonutil

import com.jaredrummler.android.shell.CommandResult
import com.jaredrummler.android.shell.Shell
import io.reactivex.rxjava3.core.Single

object ShellUtils {
    fun run(cmd: String): CommandResult {
        return Shell.SH.run(cmd)
    }

    fun runWithSuAsync(vararg cmds: String): Single<CommandResult> {
        return Single.just(cmds).map {
            if (!RootUtils.isRoot()) {
                throw ShellException("未开启root权限")
            }
            Shell.SU.run(*it)
        }
    }

    fun runWithSu(vararg cmd: String): CommandResult {
        if (!RootUtils.isRoot()) {
            return CommandResult(emptyList(), listOf("未开启root权限"), 1)
        }
        return Shell.SU.run(*cmd)
    }

    fun isRoot(): Boolean {
        return Shell.SU.available()
    }
}