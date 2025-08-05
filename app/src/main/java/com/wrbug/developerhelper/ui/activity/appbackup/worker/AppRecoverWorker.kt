package com.wrbug.developerhelper.ui.activity.appbackup.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.ExtraKey
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.createNotification
import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.ui.activity.appbackup.entity.RecoverTimeLineItem
import com.wrbug.developerhelper.util.BackupUtils
import com.wrbug.developerhelper.util.getString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppRecoverWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val data: AppRecoverWorkerData? by lazy {
        params.inputData.getString(DATA)?.fromJson()
    }

    private val workDir by lazy {
        BackupUtils.getAppBackupDir(data?.appItemInfo?.packageName.orEmpty()).absolutePath
    }
    private val tmpDir by lazy {
        File(workDir, "tmp").absolutePath
    }

    private var step = 0

    companion object {
        const val ACTION_STEP_STATUS = "ACTION_STEP_STATUS"
        const val ACTION_COMPLETED = "ACTION_COMPLETED"
        const val DATA = "data"
        private const val CHANNEL_ID = "APP_RECOVER_DEMON"
        const val TAG = "AppRecoverWorker"
        private const val NOTIFICATION_ID = 1234
    }

    private val foregroundInfo = ForegroundInfo(
        NOTIFICATION_ID, createNotification(applicationContext, CHANNEL_ID, "recover") {
            setContentTitle(R.string.app_name.getString())
            setSmallIcon(R.drawable.ic_launcher_notify)
            setContentText(R.string.recovering_app_data.getString(data?.appName.orEmpty()))
        }
    )

    override suspend fun doWork(): Result {
        setForeground(foregroundInfo)
        return withContext(Dispatchers.IO) {
            step = 0
            if (!prepareExecute()) {
                sendStatus(RecoverTimeLineItem.Status.Failed)
                sendComplete()
                return@withContext Result.success()
            }
            sendStatus(RecoverTimeLineItem.Status.Done)
            if (!recoverApk()) {
                sendStatus(RecoverTimeLineItem.Status.Failed)
                sendComplete()
                return@withContext Result.success()
            }
            sendStatus(RecoverTimeLineItem.Status.Done)
            if (!recoverData()) {
                sendStatus(RecoverTimeLineItem.Status.Failed)
                sendComplete()
                return@withContext Result.success()
            }
            if (!recoverAndroidData()) {
                sendStatus(RecoverTimeLineItem.Status.Failed)
                sendComplete()
                return@withContext Result.success()
            }
            sendStatus(RecoverTimeLineItem.Status.Done)
            sendComplete()
            Result.success()
        }
    }

    private fun recoverAndroidData(): Boolean {
        if (data?.recoverAndroidData != true) {
            return true
        }
        step++
        sendStatus(RecoverTimeLineItem.Status.Running)
        val androidDataDir = "/sdcard/Android/data/" + data?.appItemInfo?.packageName
        if (ShellManager.mkDir(androidDataDir)) {
            return false
        }
        return ShellManager.tarXF(tmpDir + "/" + data?.appItemInfo?.androidDataFile, androidDataDir)
    }

    private fun recoverData(): Boolean {
        if (data?.recoverData != true) {
            return true
        }
        step++
        sendStatus(RecoverTimeLineItem.Status.Running)
        val dataDir = Constant.getDataDir(data?.appItemInfo?.packageName.orEmpty())
        val map =
            ShellManager.getDataDirUserAndGroup(dataDir)
        if (!ShellManager.tarXF(tmpDir + "/" + data?.appItemInfo?.dataFile, "$dataDir/")) {
            return false
        }
        return ShellManager.chownDataDir(dataDir, map)
    }

    private fun recoverApk(): Boolean {
        if (data?.recoverApk != true) {
            return true
        }
        step++
        sendStatus(RecoverTimeLineItem.Status.Running)
        if (AppInfoManager.isInstalled(data?.appItemInfo?.packageName.orEmpty())) {
            val result = ShellManager.uninstallApk(data?.appItemInfo?.packageName.orEmpty())
            if (!result) {
                return false
            }
        }
        val tmpApk = "/data/local/tmp/" + data?.appItemInfo?.packageName + ".apk"
        if (!ShellManager.cpFile(tmpDir + "/" + data?.appItemInfo?.apkFile, tmpApk)) {
            return false
        }
        return ShellManager.installApk(tmpApk)
    }

    private fun prepareExecute(): Boolean {
        sendStatus(RecoverTimeLineItem.Status.Running)
        if (!ShellManager.rmFile(tmpDir)) {
            return false
        }
        if (!ShellManager.mkDir(tmpDir)) {
            return false
        }
        return ShellManager.tarXF(
            File(workDir, data?.appItemInfo?.backupFile.orEmpty()).absolutePath,
            tmpDir
        )
    }


    override suspend fun getForegroundInfo(): ForegroundInfo {
        return foregroundInfo
    }

    private fun sendStatus(status: RecoverTimeLineItem.Status) {
        applicationContext.sendBroadcast(Intent(ACTION_STEP_STATUS).apply {
            putExtra(ExtraKey.KEY_1, step to status.status)
        })
    }

    private fun sendComplete() {
        applicationContext.sendBroadcast(Intent(ACTION_COMPLETED))
    }
}