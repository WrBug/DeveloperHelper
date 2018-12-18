package com.wrbug.developerhelper.util

import com.jaredrummler.android.shell.CommandResult
import com.jaredrummler.android.shell.Shell
import com.wrbug.developerhelper.model.mmkv.ConfigKv
import com.wrbug.developerhelper.model.mmkv.manager.MMKVManager

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object ShellUtils {
    var sExecutor: Executor = Executors.newFixedThreadPool(5)
    val configKv = MMKVManager.get(ConfigKv::class.java)
    fun run(cmds: Array<String>, callback: ShellResultCallback) {
        sExecutor.execute(object : Runnable(cmds, callback) {
            override fun run() {
                val cmds = args[0] as Array<String>
                val callback = args[1] as ShellResultCallback
                val run = Shell.SH.run(*cmds)
                callback.onComplete(run)
            }
        })
    }

    fun run(vararg cmds: String): CommandResult {
        return Shell.SH.run(*cmds)
    }

    fun runWithSu(cmds: Array<String>, callback: ShellResultCallback) {
        if (!configKv.getOpenRoot()) {
            callback.onError("未开启root权限")
            return
        }
        sExecutor.execute(object : Runnable(cmds, callback) {
            override fun run() {
                val cmds = args[0] as Array<String>
                val callback = args[1] as ShellResultCallback
                val run = Shell.SU.run(*cmds)
                callback.onComplete(run)
            }
        })
    }

    fun runWithSuIgnoreSetting(vararg cmds: String): CommandResult {
        return Shell.SU.run(*cmds)
    }


    fun runWithSu(vararg cmds: String): CommandResult {
        if (configKv.getOpenRoot().not()) {
            return CommandResult(arrayListOf("未开启root权限"), arrayListOf("未开启root权限"), 1)
        }
        return Shell.SU.run(*cmds)
    }

    abstract class ShellResultCallback(vararg args: Any) {
        protected var args = args
        open fun onComplete(result: CommandResult) {

        }

        open fun onError(msg: String) {

        }
    }

}