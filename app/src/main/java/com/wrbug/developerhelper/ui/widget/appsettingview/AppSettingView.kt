package com.wrbug.developerhelper.ui.widget.appsettingview

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.showToast
import com.wrbug.developerhelper.commonutil.AppManagerUtils
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.mmkv.ConfigKv
import com.wrbug.developerhelper.mmkv.manager.MMKVManager
import kotlinx.android.synthetic.main.view_app_setting.view.*
import android.content.Intent
import android.net.Uri
import androidx.appcompat.widget.AppCompatButton
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.commonutil.zip
import com.wrbug.developerhelper.util.BackupUtils
import com.wrbug.developerhelper.commonutil.toUri
import gdut.bsx.share2.Share2
import gdut.bsx.share2.ShareContentType
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File


class AppSettingView : ScrollView {
    var apkInfo: ApkInfo? = null
    private val configKv = MMKVManager.get(ConfigKv::class.java)
    private var exportDexBtn: AppCompatButton? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_app_setting, this)
        exportDexBtn = findViewById(R.id.exportDexBtn)
        if (configKv.isOpenRoot().not()) {
            backupApkBtn.isEnabled = false
            backupApkDataDirBtn.isEnabled = false
            restartAppBtn.isEnabled = false
            stopAppBtn.isEnabled = false
            deleteAppDataBtn.isEnabled = false
            exportDexBtn?.isEnabled = false
        }
        initListener()
    }

    private fun initListener() {
        backupApkBtn.setOnClickListener {
            doBackupApk()
        }
        backupApkDataDirBtn.setOnClickListener {
            doBackupDataDir()
        }
        restartAppBtn.setOnClickListener {
            doRestartApp()
        }
        stopAppBtn.setOnClickListener {
            doStopApp()
        }
        deleteAppDataBtn.setOnClickListener {
            doDeleteAppData()
        }
        uninstallAppBtn.setOnClickListener {
            doUninstallApp()
        }

        exportDexBtn?.setOnClickListener {
            doBackupDexData()
        }
    }

    private fun doBackupDexData() {
        apkInfo?.apply {
            showToast(context.getString(R.string.packing_files))
            doAsync {
                val dir = File(context.externalCacheDir, "dex/${applicationInfo.packageName}")
                if (dir.exists()) {
                    ShellManager.rmFile(dir.absolutePath)
                }
                dir.mkdirs()
                val dexDir = "/data/data/${applicationInfo.packageName}/dump"
                val lsDir = ShellManager.lsDir(dexDir)
                if (lsDir.isEmpty()) {
                    uiThread {
                        showToast(context.getString(R.string.no_dex_files))
                    }
                    return@doAsync
                }
                if (ShellManager.cpFile(dexDir, dir.absolutePath)) {
                    val zipFile =
                        File(context.externalCacheDir, "${apkInfo?.getAppName() ?: ""}-dex.zip")
                    dir.zip(zipFile)
                    val uri = zipFile.toUri(context)
                    if (uri == null) {
                        showToast(R.string.export_failed)
                        return@doAsync
                    }
                    uiThread {
                        showShareDexNotice(uri)
                    }
                } else {
                    uiThread {
                        showToast(context.getString(R.string.export_failed))
                    }
                }
            }
        }
    }

    private fun showShareDexNotice(uri: Uri) {
        Share2.Builder(context as Activity)
            .setContentType(ShareContentType.FILE)
            .setShareFileUri(uri)
            .setOnActivityResult(10)
            .build()
            .shareBySystem()
    }

    private fun doUninstallApp() {
        apkInfo?.apply {
            AppManagerUtils.uninstallApp(context, applicationInfo.packageName)
        }
    }

    private fun doDeleteAppData() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            showNotice(
                context.getString(R.string.confirm_delete_app_data),
                DialogInterface.OnClickListener { _, _ ->
                    if (AppManagerUtils.clearAppData(applicationInfo.packageName)) {
                        activityFinish()
                        showToast(context.getString(R.string.clear_complete))
                    }
                })

        }
    }

    private fun doStopApp() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            showNotice(
                context.getString(R.string.confirm_stop_app),
                DialogInterface.OnClickListener { _, _ ->
                    if (AppManagerUtils.forceStopApp(applicationInfo.packageName)) {
                        activityFinish()
                    }
                })
        }
    }

    private fun doRestartApp() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            showNotice(
                context.getString(R.string.confirm_restart_app),
                DialogInterface.OnClickListener { _, _ ->
                    AppManagerUtils.restartApp(context, applicationInfo.packageName)
                    activityFinish()
                })
        }
    }

    private fun doBackupDataDir() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            val backupAppData =
                BackupUtils.backupAppData(applicationInfo.packageName, applicationInfo.dataDir)
            if (backupAppData == null) {
                showToast(context.getString(R.string.backup_failed))
                return
            }
            if (context !is BaseActivity) {
                showToast(context.getString(R.string.backup_success_msg))
                return
            }
            showShareDataNotice(backupAppData)
        }
    }

    private fun showShareDataNotice(backupAppData: File) {
        showNotice(
            context.getString(R.string.backup_success_and_share_msg),
            DialogInterface.OnClickListener { _, _ ->
                (context as BaseActivity).requestPermission(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    object : BaseActivity.PermissionCallback() {
                        override fun granted() {
                            val zipFile = File(
                                context.externalCacheDir,
                                "${apkInfo?.getAppName() ?: ""}-data.zip"
                            )
                            backupAppData.zip(zipFile)
                            val uri = zipFile.toUri(context)
                            if (uri == null) {
                                showToast(context.getString(R.string.share_failed))
                                return
                            }
                            activityFinish()
                            Share2.Builder(context as Activity)
                                .setContentType(ShareContentType.FILE)
                                .setShareFileUri(uri)
                                .setOnActivityResult(10)
                                .build()
                                .shareBySystem()
                        }

                    })

            })
    }

    private fun doBackupApk() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            val uri = BackupUtils.backupApk(
                applicationInfo.packageName,
                applicationInfo.publicSourceDir,
                "${getAppName()}_${packageInfo.versionName}.apk"
            )
            if (uri == null) {
                showToast(context.getString(R.string.backup_failed))
                return
            }
            if (context !is BaseActivity) {
                showToast(context.getString(R.string.backup_success_msg))
                return
            }
            showShareApkDialog(uri)

        }
    }

    private fun showShareApkDialog(uri: Uri) {
        showNotice(
            context.getString(R.string.backup_success_and_share_msg),
            DialogInterface.OnClickListener { _, _ ->
                (context as BaseActivity).requestPermission(arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                    object : BaseActivity.PermissionCallback() {
                        override fun granted() {
                            activityFinish()
                            Share2.Builder(context as Activity)
                                .setContentType(ShareContentType.FILE)
                                .setShareFileUri(uri)
                                .setOnActivityResult(10)
                                .build()
                                .shareBySystem()
                        }

                    })

            })
    }

    private fun checkRoot(): Boolean {
        if (configKv.isOpenRoot().not()) {
            showToast(context.getString(R.string.please_open_root))
            return false
        }
        return true
    }

    fun activityFinish() {
        if (context is Activity) {
            (context as Activity).finish()
        }
    }

    private fun showNotice(msg: String, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(context)
            .setTitle(R.string.notice)
            .setMessage(msg)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok, listener)
            .create().show()
    }
}