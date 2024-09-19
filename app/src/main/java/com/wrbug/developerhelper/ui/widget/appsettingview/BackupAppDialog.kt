package com.wrbug.developerhelper.ui.widget.appsettingview

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.ExtraKey
import com.wrbug.developerhelper.base.versionCodeLong
import com.wrbug.developerhelper.commonutil.addTo
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.observeOnMain
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener
import com.wrbug.developerhelper.databinding.DialogBackupAppBinding
import com.wrbug.developerhelper.model.entity.BackupAppItemInfo
import com.wrbug.developerhelper.ui.widget.backupprogress.BackupProgressView
import com.wrbug.developerhelper.util.BackupUtils
import com.wrbug.developerhelper.util.format
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class BackupAppDialog : BottomSheetDialogFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager,
            apkInfo: ApkInfo?,
            backupApk: Boolean,
            backupData: Boolean,
            backupAndroidData: Boolean
        ) {
            BackupAppDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ExtraKey.DATA, apkInfo)
                    putBoolean(ExtraKey.KEY_1, backupApk)
                    putBoolean(ExtraKey.KEY_2, backupData)
                    putBoolean(ExtraKey.KEY_3, backupAndroidData)
                }
            }.show(fragmentManager, "BackupAppDialog")
        }
    }

    private lateinit var binding: DialogBackupAppBinding
    private lateinit var disposable: CompositeDisposable
    private lateinit var dateDir: String
    private var backupTimeStamp: Long = 0

    private val apkInfo: ApkInfo? by lazy {
        arguments?.getParcelable(ExtraKey.DATA)
    }

    private val backupApk by lazy {
        arguments?.getBoolean(ExtraKey.KEY_1) ?: false
    }


    private val backupData by lazy {
        arguments?.getBoolean(ExtraKey.KEY_2) ?: false
    }


    private val backupAndroidData by lazy {
        arguments?.getBoolean(ExtraKey.KEY_3) ?: false
    }

    private var successCount = 0
        set(value) {
            field = value
            checkIsAllSuccess()
        }
    private var hasError = false
        set(value) {
            field = value
            checkIsAllSuccess()
        }

    private fun checkIsAllSuccess() {
        if (hasError) {
            binding.tvNotice.isVisible = false
            binding.btnExit.isVisible = true
            binding.zipFileProgress.setStatus(BackupProgressView.Status.Canceled)
            return
        }
        if (successCount < 3) {
            return
        }
        createInfoFile()
    }

    private fun createInfoFile() {
        binding.zipFileProgress.setStatus(BackupProgressView.Status.Processing)
        runOnIo().map { apkInfo ->
            BackupUtils.zipBackupFile(apkInfo.packageInfo.packageName.orEmpty(), dateDir)
                ?.let { apkInfo to it }
                ?: throw Exception()
        }.map {
            val info = BackupAppItemInfo(
                it.second.name,
                backupApk,
                backupData,
                backupAndroidData,
                it.first.generateBackupApkFileName(),
                it.first.packageInfo.versionName.orEmpty(),
                it.first.packageInfo.versionCodeLong,
                it.first.packageInfo.packageName.orEmpty(),
                backupTimeStamp
            )
            val success =
                BackupUtils.saveBackupInfo(it.first, info, it.second.name)
            if (success) {
                return@map it.second.absolutePath
            }
            throw Exception()
        }.observeOnMain().subscribe({
            binding.zipFileProgress.setStatus(BackupProgressView.Status.Success, it)
            showSuccess()
        }, {
            hasError = true
            binding.zipFileProgress.setStatus(BackupProgressView.Status.Failed)
        }).addTo(disposable)
    }

    private fun showSuccess() {
        binding.btnExit.isVisible = true
        binding.tvNotice.text = getString(
            R.string.backup_success_notice,
            BackupUtils.getAppBackupDir(
                apkInfo?.packageInfo?.packageName.orEmpty()
            ).absolutePath
        )
        binding.tvNotice.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.material_color_green_600
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        successCount = 0
        backupTimeStamp = System.currentTimeMillis()
        dateDir = backupTimeStamp.format("yyyy-MM-dd-HH_mm_ss")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        disposable = CompositeDisposable()
        binding = DialogBackupAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apkProgress.setTitle(R.string.item_backup_apk)
        binding.androidDataProgress.setTitle(R.string.item_backup_android_data)
        binding.dataProgress.setTitle(R.string.item_backup_data)
        binding.zipFileProgress.setTitle(R.string.item_zip_backup_file)
        binding.zipFileProgress.setStatus(BackupProgressView.Status.Waiting)
        binding.btnExit.setOnDoubleCheckClickListener {
            dismissAllowingStateLoss()
        }
        backupApk()
        backupData()
        backupAndroidData()
    }

    private fun backupApk() {
        if (!backupApk) {
            successCount++
            binding.apkProgress.setStatus(BackupProgressView.Status.Ignore)
            return
        }
        binding.apkProgress.setStatus(BackupProgressView.Status.Processing)
        runOnIo().map {
            BackupUtils.backupApk(
                it.applicationInfo.packageName,
                dateDir,
                it.applicationInfo.publicSourceDir,
                it.generateBackupApkFileName()
            ) ?: throw Exception()
        }.observeOnMain().subscribe({
            successCount++
            binding.apkProgress.setStatus(BackupProgressView.Status.Success, it)
        }, {
            hasError = true
            binding.apkProgress.setStatus(BackupProgressView.Status.Failed)
        }).addTo(disposable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hasError) {
            ShellManager.rmFile(
                BackupUtils.getCurrentAppBackupDir(
                    apkInfo?.packageInfo?.packageName.orEmpty(),
                    dateDir
                ).absolutePath
            )
        }
    }

    private fun backupAndroidData() {
        if (!backupAndroidData) {
            successCount++
            binding.androidDataProgress.setStatus(BackupProgressView.Status.Ignore)
            return
        }
        binding.androidDataProgress.setStatus(BackupProgressView.Status.Processing)
        runOnIo().map {
            BackupUtils.backupAppAndroidData(dateDir, it.applicationInfo.packageName)
                ?: throw Exception()
        }.observeOnMain().subscribe({
            successCount++
            binding.androidDataProgress.setStatus(
                BackupProgressView.Status.Success,
                it.ifEmpty { getString(R.string.no_need_to_backup) }
            )
        }, {
            hasError = true
            binding.androidDataProgress.setStatus(BackupProgressView.Status.Failed)
        }).addTo(disposable)
    }

    private fun backupData() {
        if (!backupData) {
            successCount++
            binding.dataProgress.setStatus(BackupProgressView.Status.Ignore)
            return
        }
        binding.dataProgress.setStatus(BackupProgressView.Status.Processing)
        runOnIo().map {
            BackupUtils.backupAppData(dateDir, it.applicationInfo.packageName) ?: throw Exception()
        }.observeOnMain().subscribe({
            successCount++
            binding.dataProgress.setStatus(BackupProgressView.Status.Success, it.absolutePath)
        }, {
            hasError = true
            binding.dataProgress.setStatus(BackupProgressView.Status.Failed)
        }).addTo(disposable)
    }


    private fun runOnIo() = if (apkInfo == null) {
        Single.error(Exception())
    } else {
        Single.just(apkInfo!!).subscribeOn(Schedulers.newThread())
    }
}