package com.wrbug.developerhelper.commonutil

import com.jaredrummler.android.shell.CommandResult
import com.jaredrummler.android.shell.Shell
import org.jetbrains.anko.doAsync

object ShellUtils {
    fun run(cmds: Array<String>, callback: ShellResultCallback) {
        doAsync {
            val run = Shell.SH.run(*cmds)
            callback.onComplete(run)
        }
    }

    fun run(vararg cmds: String): CommandResult {
        return Shell.SH.run(*cmds)
    }

    fun runWithSu(cmds: Array<String>, callback: ShellResultCallback) {
        if (!RootUtils.isRoot()) {
            callback.onError("未开启root权限")
            return
        }
        doAsync {
            val run = Shell.SU.run(*cmds)
            callback.onComplete(run)
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

    abstract class ShellResultCallback(vararg args: Any) {
        protected var args = args
        open fun onComplete(result: CommandResult) {

        }

        open fun onError(msg: String) {

        }
    }

}