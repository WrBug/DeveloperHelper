package com.wrbug.developerhelper.commonutil

import android.app.Application
import android.content.Context
import com.wrbug.developerhelper.commonutil.entity.CpuABI
import com.wrbug.developerhelper.commonutil.shell.ShellUtils
import org.jetbrains.anko.doAsync


object CommonUtils {
    lateinit var application: Application
    fun register(ctx: Context) {
        application = ctx.applicationContext as Application
        releaseBusyBox(ctx)
    }

    private fun releaseBusyBox(ctx: Context) {
        doAsync {
            val name = when (getCPUABI()) {
                CpuABI.ARM -> "busybox_arm64"
                CpuABI.X86 -> "busybox_x86_64"
            }
            val data = ctx.resources.assets.open(name).readBytes()
            val file = ShellUtils.busyBoxFile
            file.writeBytes(data)
            ShellUtils.run("chmod +x " + file.absolutePath)
        }
    }


    private fun getCPUABI(): CpuABI {
        val result = ShellUtils.run("getprop ro.product.cpu.abi")
        if (result.isSuccessful.not()) {
            return CpuABI.ARM
        }
        return if (result.getStdout().contains("x86")) {
            CpuABI.X86
        } else {
            CpuABI.ARM
        }
    }
}