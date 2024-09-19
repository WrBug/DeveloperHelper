package com.wrbug.developerhelper.commonutil.shell

import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.entity.LsFileInfo
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.commonutil.runOnIO
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.util.regex.Pattern

object ShellManager {

    private const val SHELL_TOP_ACTIVITY = "dumpsys activity top"
    private const val SHELL_APP_ACTIVITY = "dumpsys activity %1\$s"
    private const val SHELL_PROCESS_PID_1 =
        "ps -ef | grep \"%1\$s\" | grep -v %1\$s:| grep -v grep | awk '{print \$2}'"
    private const val SHELL_PROCESS_PID_2 = "top -b -n 1 |grep %1\$s |grep -v grep|grep -v %1\$s:"
    private const val SHELL_PROCESS_PID_3 = "top -n 1 |grep %1\$s |grep -v grep|grep -v %1\$s:"
    private var SHELL_OPEN_ACCESSiBILITY_SERVICE = arrayOf(
        "settings put secure enabled_accessibility_services com.wrbug.developerhelper/com.wrbug.developerhelper.service.DeveloperHelperAccessibilityService",
        "settings put secure accessibility_enabled 1"
    )
    private const val SHELL_LS_FILE = "ls -l %1\$s"
    private const val SHELL_CHECK_IS_SQLITE = "od -An -tx %1\$s  |grep '694c5153'"
    private const val SHELL_UNINSTALL_APP = "pm uninstall %1\$s"
    private const val SHELL_CLEAR_APP_DATA = "pm clear %1\$s"
    private const val SHELL_FORCE_STOP_APP = "am force-stop %1\$s"
    private val SHELL_OPEN_ADB_WIFI =
        arrayOf("setprop service.adb.tcp.port 5555", "stop adbd", "start adbd")

    fun getTopActivity(): Single<TopActivityInfo> {
        return ShellUtils.runWithSuAsync(SHELL_TOP_ACTIVITY, useBusyBox = false).map {
            getTopActivity(it)
        }.runOnIO()
    }

    private fun tabCount(str: String): Int {
        return (str.length - str.trimStart().length) / 2
    }

    private fun getTopActivity(result: CommandResult): TopActivityInfo {
        val stdout = result.getStdout()
        val topActivityInfo = TopActivityInfo()
        val task_s = stdout.split("TASK ")
        for (task_ in task_s) {
            if (task_.contains("mResumed=true")) {
                val regex = "ACTIVITY .* [0-9a-fA-F]+ pid.*"
                val pattern = Pattern.compile(regex)
                val matcher = pattern.matcher(task_)
                if (matcher.find()) {
                    topActivityInfo.setFullActivity(matcher.group().split(" ")[1])
                }
                val split = task_.split("\n[ ]{4}[A-Z]".toRegex()).dropLastWhile { it.isEmpty() }
                for (s in split) {
                    if (s.contains("iew Hierarchy")) {
                        for (s1 in s.split("\n".toRegex()).dropLastWhile { it.isEmpty() }) {
                            if (s1.matches(".*\\{.*\\}.*".toRegex())) {
                                val data = s1.substring(s1.indexOf("{") + 1, s1.lastIndexOf("}"))
                                val split1 =
                                    data.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                if (split1.size != 6 || split1[5].contains("id/").not()) {
                                    continue
                                }
                                topActivityInfo.viewIdHex[split1[5].substring(split1[5].indexOf("id/"))] =
                                    split1[4]
                            }
                        }
                    }
                }
                break
            }
        }
        return topActivityInfo
    }

    fun lsFile(file: String): LsFileInfo? {
        val result: CommandResult = ShellUtils.runWithSu(String.format(SHELL_LS_FILE, file))
        if (result.isSuccessful.not()) {
            return null
        }
        val split = result.getStdout().split(" ")
        if (split.size <= 4) {
            return null
        }
        val info = LsFileInfo()
        info.permission = split[0]
        info.user = split[2]
        info.group = split[3]
        return info
    }

    fun getPid(packageName: String): String {
        var result: CommandResult =
            ShellUtils.runWithSu(
                String.format(SHELL_PROCESS_PID_1, packageName),
                useBusyBox = false
            )
        if (result.isSuccessful) {
            return result.getStdout()
        }
        result = ShellUtils.runWithSu(
            String.format(SHELL_PROCESS_PID_2, packageName),
            useBusyBox = false
        )
        if (result.isSuccessful.not()) {
            result = ShellUtils.runWithSu(
                String.format(SHELL_PROCESS_PID_3, packageName),
                useBusyBox = false
            )
        }
        if (result.isSuccessful.not()) {
            return ""
        }
        return result.getStdout().trim().split(" ")[0]
    }

    fun getSqliteFiles(packageName: String): Array<File> {
        return getSqliteFiles(Constant.dataDir, packageName)
    }

    private fun getSqliteFiles(dir: String, packageName: String): Array<File> {
        val dbPath = "$dir/$packageName/databases"
        val list = lsDir(dbPath)
        val files = ArrayList<File>()
        for (file in list) {
            file?.run {
                val cmd = String.format(SHELL_CHECK_IS_SQLITE, "$dbPath/$file")
                val result = ShellUtils.runWithSu(cmd)
                if (result.isSuccessful && result.getStdout().isNullOrEmpty().not()) {
                    files.add(File(dbPath, file))

                }
            }
        }
        return files.toTypedArray()
    }

    fun openAccessibilityService(): Single<Boolean> {
        return ShellUtils.runWithSuAsync(*SHELL_OPEN_ACCESSiBILITY_SERVICE, useBusyBox = false)
            .map {
                it.isSuccessful && it.getStdout().isEmpty()
            }.onErrorReturn { false }.runOnIO()
    }

    fun catFile(filaPath: String): String {
        val result = ShellUtils.runWithSu("cat $filaPath")
        return result.getStdout()
    }

    fun rmFile(file: String): Boolean {
        val result = ShellUtils.runWithSu("rm -rf $file")
        return result.isSuccessful
    }

    fun modifyFile(filaPath: String, content: String): Boolean {
        val result = ShellUtils.runWithSu("echo $content >> $filaPath")
        return result.isSuccessful
    }

    fun cpFile(source: String, dst: String, mod: String = "666"): Boolean {
        val dir = dst.substring(0, dst.lastIndexOf("/"))
        var result = ShellUtils.runWithSu("mkdir -p $dir")
        if (result.isSuccessful.not()) {
            return false
        }
        result = ShellUtils.runWithSu("cp -R $source $dst && chmod $mod $dst")
        return result.isSuccessful || result.getStderr()
            .contains("Operation not permitted")
    }

    fun tarCF(tarPath: String, srcPath: String): Boolean {
        val dir = tarPath.substring(0, tarPath.lastIndexOf("/"))
        var result = ShellUtils.runWithSu("mkdir -p $dir")
        if (result.isSuccessful.not()) {
            return false
        }
        result = ShellUtils.runWithSu("tar -pcf  $tarPath -C $srcPath .")
        return result.isSuccessful || result.getStderr()
            .contains("Operation not permitted")
    }


    fun catFile(source: String, dst: String, mod: String? = null): Boolean {
        val cmds = arrayListOf<String>()
        cmds.add("cat $source  > $dst")
        if (mod != null) {
            cmds.add("chmod $mod $dst")
        }
        val result = ShellUtils.runWithSu(*(cmds.toTypedArray()))
        return result.isSuccessful
    }


    fun lsDir(path: String): List<String?> {
        val result = ShellUtils.runWithSu("ls $path")
        return result.stdout
    }

    fun clearAppData(packageName: String): Boolean {
        val result = ShellUtils.runWithSu(
            String.format(SHELL_CLEAR_APP_DATA, packageName),
            useBusyBox = false
        )
        return result.isSuccessful
    }

    fun forceStopApp(packageName: String): Boolean {
        val result = ShellUtils.runWithSu(
            String.format(SHELL_FORCE_STOP_APP, packageName),
            useBusyBox = false
        )
        return result.isSuccessful
    }

    fun openAdbWifi(): Boolean {
//        val result = ShellUtils.runWithSu(*SHELL_OPEN_ADB_WIFI)
        return false
    }
}
