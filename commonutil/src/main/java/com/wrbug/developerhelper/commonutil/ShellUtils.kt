package com.wrbug.developerhelper.commonutil

import com.jaredrummler.ktsh.Shell
import io.reactivex.rxjava3.core.Single

object ShellUtils {
    fun run(cmd: String): Shell.Command.Result {
        return Shell.SH.run(cmd)
    }

    fun runWithSuAsync(cmds: Array<String>): Single<Shell.Command.Result> {
        return runWithSuAsync(cmds.joinToString(" && "))
    }

    fun runWithSuAsync(cmds: String): Single<Shell.Command.Result> {
        return Single.just(cmds).map {
            if (!RootUtils.isRoot()) {
                throw ShellException("未开启root权限")
            }
            Shell.SU.run(it)
        }
    }

    fun runWithSu(vararg cmd: String): Shell.Command.Result {
        return runWithSu(cmd.joinToString(" && "))
    }

    fun runWithSu(cmd: String): Shell.Command.Result {
        return Shell.SU.run(cmd)
    }

    fun isRoot(): Boolean {
        return Shell.SU.isAlive()
    }
}