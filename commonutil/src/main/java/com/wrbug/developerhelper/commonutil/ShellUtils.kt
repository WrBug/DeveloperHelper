package com.wrbug.developerhelper.commonutil

import com.jaredrummler.android.shell.CommandResult
import com.jaredrummler.android.shell.Shell
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.jetbrains.anko.doAsync

object ShellUtils {
    fun run(vararg cmds: String): CommandResult {
        return Shell.SH.run(*cmds)
    }

    fun runWithSu(cmds: Array<String>): Single<CommandResult> {
        return Single.just(cmds).map {
            if (!RootUtils.isRoot()) {
                throw ShellException("未开启root权限")
            }
            Shell.SU.run(*it)
        }
    }

    fun runWithSuIgnoreSetting(vararg cmds: String): CommandResult {
        return Shell.SU.run(*cmds)
    }


    fun runWithSu(vararg cmds: String): CommandResult {
        if (RootUtils.isRoot().not()) {
            return CommandResult(arrayListOf("未开启root权限"), arrayListOf("未开启root权限"), 1)
        }
        return Shell.SU.run(*cmds)
    }

    fun isRoot(): Boolean {
        return Shell.SU.available()
    }
}