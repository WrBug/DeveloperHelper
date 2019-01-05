package com.wrbug.developerhelper.util
import com.wrbug.developerhelper.commonutil.shell.ShellManager

object EnforceUtils {
    fun getEnforceType(packageName: String): EnforceType {
        val files = ShellManager.getZipFileList(ShellManager.findApkDir(packageName)).toString().trim()
        return when {
            isIjiaMi(files) -> EnforceType.I_JIA_MI
            is360(files) -> EnforceType.QI_HOO
            isLeGu(files) -> EnforceType.LE_GU
            isBangcle(files) -> EnforceType.BANGCLE
            else -> EnforceType.UN_KNOWN
        }
    }

    private fun isIjiaMi(files: String): Boolean {
        return files.contains("ijm") or files.contains("ijiami")

    }

    private fun is360(files: String): Boolean {
        return files.contains("libjiagu.so")
    }

    private fun isLeGu(files: String): Boolean {
        return files.contains("tencent_stub")
    }

    private fun isBangcle(files: String): Boolean {
        return files.contains("bangcle")
    }


    enum class EnforceType(val type: String) {
        QI_HOO("360"),
        BAI_DU("百度"),
        I_JIA_MI("爱加密"),
        LE_GU("乐固"),
        BANGCLE("梆梆"),
        UN_KNOWN("未知/无加固"),
    }
}