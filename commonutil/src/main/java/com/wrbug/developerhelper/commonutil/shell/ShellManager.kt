package com.wrbug.developerhelper.commonutil.shell

import com.jaredrummler.android.shell.CommandResult
import com.wrbug.developerhelper.commonutil.CommonUtils
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.ShellUtils
import com.wrbug.developerhelper.commonutil.entity.FragmentInfo
import com.wrbug.developerhelper.commonutil.entity.LsFileInfo
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.commonutil.toInt
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
    private const val SHELL_GET_ZIP_FILE_LIST =
        "app_process -Djava.class.path=/data/local/tmp/zip.dex /data/local/tmp Zip %s"
    private const val SHELL_CHECK_IS_SQLITE = "od -An -tx %1\$s  |grep '694c5153'"
    private const val SHELL_UNINSTALL_APP = "pm uninstall %1\$s"
    private const val SHELL_CLEAR_APP_DATA = "pm clear %1\$s"
    private const val SHELL_FORCE_STOP_APP = "am force-stop %1\$s"
    private val SHELL_OPEN_ADB_WIFI =
        arrayOf("setprop service.adb.tcp.port 5555", "stop adbd", "start adbd")

    fun getTopActivity(callback: Callback<TopActivityInfo?>) {
        ShellUtils.runWithSu(arrayOf(SHELL_TOP_ACTIVITY),
            object : ShellUtils.ShellResultCallback() {
                override fun onComplete(result: CommandResult) {
                    val activityInfo = getTopActivity(result)
                    activityInfo.fragments = getFragment(activityInfo.packageName)
                    callback.onSuccess(activityInfo)
                }

                override fun onError(msg: String) {
                    callback.onSuccess(null)
                }
            })

    }

    private fun getFragment(packageName: String): Array<FragmentInfo> {
        val lines =
            ShellUtils.runWithSu(String.format(SHELL_APP_ACTIVITY, packageName)).stdout.toList()
        val splitIndex = lines.indexOfFirst { it.trim().isEmpty() }
        val (tasks, insetsController) = if (splitIndex == -1) {
            lines to emptyList()
        } else {
            lines.subList(0, splitIndex) to lines.subList(splitIndex + 1, lines.size)
        }

        val list = ArrayList<FragmentInfo>()
        val map = getFragments(insetsController.ifEmpty { tasks })
        map.forEach {
            val info = FragmentInfo(
                name = it.key,
                containerId = it.value["mContainerId"].orEmpty(),
                tag = it.value["mTag"].orEmpty(),
                state = it.value["mState"].toInt(),
                who = it.value["mWho"].orEmpty(),
                backStackNesting = it.value["mBackStackNesting"].toInt(),
                added = it.value["mAdded"].toBoolean(),
                removing = it.value["mRemoving"].toBoolean(),
                fromLayout = it.value["mFromLayout"].toBoolean(),
                inLayout = it.value["mInLayout"].toBoolean(),
                hidden = it.value["mHidden"].toBoolean(),
                detached = it.value["mDetached"].toBoolean(),
            )
            list.add(info)
        }
        return list.toTypedArray()
    }

    private fun getFragments(list: List<String>): Map<String, Map<String, String>> {
        val index = list.indexOfFirst { tabCount(it) == 2 && it.trim() == "Added Fragments:" }
        if (index == -1) {
            return emptyMap()
        }
        val fragmentNameList = arrayListOf<String>()
        for (i in index + 1 until list.size) {
            val line = list[i]
            if (tabCount(line) != 3) {
                break
            }
            fragmentNameList.add(line.trim().split(" ")[1])
        }
        val fragmentMap = hashMapOf<String, Map<String, String>>()
        list.forEachIndexed { index, str ->
            if (tabCount(str) == 2) {
                val name = str.trim().split(" ")[0]
                if (!fragmentNameList.contains(name)) {
                    return@forEachIndexed
                }
                val map = hashMapOf<String, String>()
                for (i in index + 1 until list.size) {
                    val line = list[i].trim()
                    if (!line.startsWith("m")) {
                        break
                    }
                    line.split(" ").forEach {
                        val pair = it.split("=")
                        if (pair.size == 2) {
                            map[pair[0]] = pair[1]
                        }
                    }
                }
                val pureName = name.substring(0, name.indexOf("{"))
                if (pureName != "ReportFragment") {
                    fragmentMap[pureName] = map
                }
            }
        }
        return fragmentMap
    }

    private fun tabCount(str: String): Int {
        return (str.length - str.trimStart().length) / 2
    }

    fun getTopActivity(result: CommandResult): TopActivityInfo {
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
            ShellUtils.runWithSu(String.format(SHELL_PROCESS_PID_1, packageName))
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

    fun getSqliteFiles(packageName: String): Array<File> {
        val list = getSqliteFiles("/data/data", packageName)
        if (list.isNotEmpty()) {
            return list
        }
        return getSqliteFiles(Constant.DATA_MIRROR, packageName)
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

    fun openAccessibilityService(callback: Callback<Boolean>? = null) {
        ShellUtils.runWithSu(SHELL_OPEN_ACCESSiBILITY_SERVICE,
            object : ShellUtils.ShellResultCallback() {
                override fun onComplete(result: CommandResult) {
                    callback?.onSuccess(result.isSuccessful && result.getStdout().isEmpty())
                }

                override fun onError(msg: String) {
                    callback?.onSuccess(false)
                }
            })
    }

    fun catFile(filaPath: String): String {
        val commandResult = ShellUtils.runWithSu("cat $filaPath")
        return commandResult.getStdout()
    }

    fun rmFile(file: String): Boolean {
        val commandResult = ShellUtils.runWithSu("rm -rf $file")
        return commandResult.isSuccessful
    }

    fun modifyFile(filaPath: String, content: String): Boolean {
        val commandResult = ShellUtils.runWithSu("echo $content >> $filaPath")
        return commandResult.isSuccessful
    }

    fun cpFile(source: String, dst: String, mod: String = "666"): Boolean {
        val dir = dst.substring(0, dst.lastIndexOf("/"))
        var commandResult = ShellUtils.runWithSu("mkdir -p $dir")
        if (commandResult.isSuccessful.not()) {
            return false
        }
        commandResult = ShellUtils.runWithSu("cp -R $source $dst && chmod $mod $dst")
        return commandResult.isSuccessful || commandResult.getStderr()
            ?.contains("Operation not permitted") ?: false
    }

    fun catFile(source: String, dst: String, mod: String? = null): Boolean {
        val cmds = arrayListOf<String>()
        cmds.add("cat $source  > $dst")
        if (mod != null) {
            cmds.add("chmod $mod $dst")
        }
        val commandResult = ShellUtils.runWithSu(*(cmds.toTypedArray()))
        return commandResult.isSuccessful
    }

    fun getZipFileList(path: String): List<String?> {
        val file = File(CommonUtils.application.cacheDir, "zip.dex")
        if (file.exists()) {
            ShellUtils.runWithSu(
                "cp ${file.absolutePath} /data/local/tmp", "rm -rf ${file.absolutePath}"
            )
        }
        val commandResult = ShellUtils.runWithSu(String.format(SHELL_GET_ZIP_FILE_LIST, path))
        return commandResult.stdout
    }

    fun lsDir(path: String): List<String?> {
        val commandResult = ShellUtils.runWithSu("ls $path")
        return commandResult.stdout
    }

    fun findApkDir(packageName: String): String {
        val cmd = "ls /data/app/|grep $packageName"
        val dir = ShellUtils.runWithSu(cmd).getStdout()
        return "/data/app/$dir/base.apk"
    }

    fun uninstallApp(packageName: String): Boolean {
        val commandResult = ShellUtils.runWithSu(String.format(SHELL_UNINSTALL_APP, packageName))
        return commandResult.isSuccessful
    }

    fun clearAppData(packageName: String): Boolean {
        val commandResult = ShellUtils.runWithSu(String.format(SHELL_CLEAR_APP_DATA, packageName))
        return commandResult.isSuccessful
    }

    fun forceStopApp(packageName: String): Boolean {
        val commandResult = ShellUtils.runWithSu(String.format(SHELL_FORCE_STOP_APP, packageName))
        return commandResult.isSuccessful
    }

    fun openAdbWifi(): Boolean {
        val commandResult = ShellUtils.runWithSu(*SHELL_OPEN_ADB_WIFI)
        return commandResult.isSuccessful
    }
}
