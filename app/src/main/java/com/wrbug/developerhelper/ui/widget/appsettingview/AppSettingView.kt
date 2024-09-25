package com.wrbug.developerhelper.ui.widget.appsettingview

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.wrbug.developerhelper.BuildConfig
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.requestStoragePermission
import com.wrbug.developerhelper.base.showToast
import com.wrbug.developerhelper.commonutil.AppManagerUtils
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.util.setOnRootCheckClickListener
import com.wrbug.developerhelper.util.visible
import com.wrbug.developerhelper.databinding.DialogBackupAppSelectBinding
import com.wrbug.developerhelper.databinding.ViewAppSettingBinding
import com.wrbug.developerhelper.mmkv.ConfigKv
import com.wrbug.developerhelper.mmkv.manager.MMKVManager
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AppSettingView : ScrollView {

    private var apkInfo: ApkInfo? = null
    private val configKv = MMKVManager.get(ConfigKv::class.java)
    private lateinit var binding: ViewAppSettingBinding
    private lateinit var disposable: CompositeDisposable

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    fun setApkInfo(apkInfo: ApkInfo?) {
        this.apkInfo = apkInfo
        if (apkInfo?.applicationInfo?.packageName == BuildConfig.APPLICATION_ID) {
            binding.uninstallAppBtn.visible = false
        }
    }

    private fun initView() {
        binding = ViewAppSettingBinding.inflate(LayoutInflater.from(context), this, true)
        initListener()
    }

    private fun initListener() {
        binding.backupAppBtn.setOnRootCheckClickListener {
            showBackupSelect()
        }
        binding.restoreAppBtn.setOnRootCheckClickListener {
        }
        binding.restartAppBtn.setOnRootCheckClickListener {
            doRestartApp()
        }
        binding.stopAppBtn.setOnRootCheckClickListener {
            doStopApp()
        }
        binding.deleteAppDataBtn.setOnRootCheckClickListener {
            doDeleteAppData()
        }
        binding.uninstallAppBtn.setOnRootCheckClickListener {
            doUninstallApp()
        }
    }

    private fun showBackupSelect() {
        context.requestStoragePermission {
            val selected = booleanArrayOf(false, false, false)
            val binding = DialogBackupAppSelectBinding.inflate(LayoutInflater.from(context))
            binding.cbApk.setOnCheckedChangeListener { _, isChecked ->
                selected[0] = isChecked
            }
            binding.cbData.setOnCheckedChangeListener { _, isChecked ->
                selected[1] = isChecked
            }
            binding.cbAndroidData.setOnCheckedChangeListener { _, isChecked ->
                selected[2] = isChecked
            }
            AlertDialog.Builder(context)
                .setTitle(R.string.backup_app_file)
                .setView(binding.root)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(
                    R.string.ok
                ) { _, _ ->
                    doBackup(selected)
                }.create().show()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        disposable = CompositeDisposable()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposable.dispose()
    }

    private fun doBackup(selected: BooleanArray) {
        val activity = context as? FragmentActivity ?: return
        if (selected.find { it } == null) {
            return
        }
        BackupAppDialog.show(
            activity.supportFragmentManager,
            apkInfo,
            selected[0],
            selected[1],
            selected[2]
        )
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
                context.getString(R.string.confirm_delete_app_data)
            ) {
                if (AppManagerUtils.clearAppData(applicationInfo.packageName)) {
                    activityFinish()
                    showToast(context.getString(R.string.clear_complete))
                }
            }

        }
    }

    private fun doStopApp() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            showNotice(context.getString(R.string.confirm_stop_app)) {
                if (AppManagerUtils.forceStopApp(applicationInfo.packageName)) {
                    activityFinish()
                }
            }
        }
    }

    private fun doRestartApp() {
        if (checkRoot().not()) {
            return
        }
        apkInfo?.apply {
            showNotice(
                context.getString(R.string.confirm_restart_app)
            ) {
                AppManagerUtils.restartApp(context, applicationInfo.packageName)
                activityFinish()
            }
        }
    }

    private fun checkRoot(): Boolean {
        if (configKv.isOpenRoot().not()) {
            showToast(context.getString(R.string.please_open_root))
            return false
        }
        return true
    }

    private fun activityFinish() {
        if (context is Activity) {
            (context as Activity).finish()
        }
    }

    private fun showNotice(msg: String, listener: () -> Unit) {
        AlertDialog.Builder(context).setTitle(R.string.notice).setMessage(msg)
            .setNegativeButton(R.string.cancel, null).setPositiveButton(
                R.string.ok
            ) { _, _ -> listener() }.create().show()
    }

}