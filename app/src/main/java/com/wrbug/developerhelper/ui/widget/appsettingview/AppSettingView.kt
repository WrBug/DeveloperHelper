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
import android.os.Environment
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.commonutil.zip
import com.wrbug.developerhelper.util.BackupUtils
import com.wrbug.developerhelper.util.toUri
import gdut.bsx.share2.Share2
import gdut.bsx.share2.ShareContentType
import java.io.File


class AppSettingView : ScrollView {
    var apkInfo: ApkInfo? = null
    private val configKv = MMKVManager.get(ConfigKv::class.java)

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_app_setting, this)
        if (configKv.isOpenRoot().not()) {
            backupApkBtn.isEnabled = false
            backupApkDataDirBtn.isEnabled = false
            restartAppBtn.isEnabled = false
            stopAppBtn.isEnabled = false
            deleteAppDataBtn.isEnabled = false
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
            showNotice(context.getString(R.string.confirm_delete_app_data), DialogInterface.OnClickListener { _, _ ->
                if (AppManagerUtils.clearAppData(applicationInfo.packageName)) {
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
            showNotice(context.getString(R.string.confirm_stop_app), DialogInterface.OnClickListener { _, _ ->
                AppManagerUtils.forceStopApp(applicationInfo.packageName)
            })
        }
    }

    private fun doRestartApp() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            showNotice(context.getString(R.string.confirm_restart_app), DialogInterface.OnClickListener { _, _ ->
                if (!AppManagerUtils.forceStopApp(applicationInfo.packageName)) {
                    showToast(context.getString(R.string.restart_failed))
                    return@OnClickListener
                }
                val intent = context.packageManager.getLaunchIntentForPackage(applicationInfo.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            })
        }
    }

    private fun doBackupDataDir() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            val backupAppData = BackupUtils.backupAppData(applicationInfo.packageName, applicationInfo.dataDir)
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
                            val zipFile = File(context.externalCacheDir, "${apkInfo?.getAppName() ?: ""}-data.zip")
                            backupAppData.zip(zipFile)
                            val uri = zipFile.toUri()
                            if (uri == null) {
                                showToast(context.getString(R.string.share_failed))
                                return
                            }
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

    private fun showNotice(msg: String, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(context)
            .setTitle(R.string.notice)
            .setMessage(msg)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok, listener)
            .create().show()
    }
}