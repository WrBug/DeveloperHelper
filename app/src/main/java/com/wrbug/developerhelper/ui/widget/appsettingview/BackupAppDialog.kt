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
import com.wrbug.developerhelper.basecommon.ExtraKey
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.addTo
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.observeOnMain
import com.wrbug.developerhelper.commonutil.runOnIO
import com.wrbug.developerhelper.commonwidget.util.setOnDoubleCheckClickListener
import com.wrbug.developerhelper.databinding.DialogBackupAppBinding
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

    private fun checkIsAllSuccess() {
        if (successCount < 3) {
            return
        }
        binding.btnExit.isVisible = true
        binding.tvNotice.text = getString(
            R.string.backup_success_notice,
            BackupUtils.backupDir.absolutePath + "/" + apkInfo?.applicationInfo?.packageName + "/" + dateDir
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
        dateDir = System.currentTimeMillis().format("yyyy-MM-dd-HH_mm_ss")
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
            successCount++
            binding.apkProgress.setStatus(BackupProgressView.Status.Failed)
        }).addTo(disposable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.dispose()
    }

    private fun backupAndroidData() {
        if (!backupAndroidData) {
            successCount++
            binding.androidDataProgress.setStatus(BackupProgressView.Status.Ignore)
            return
        }
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
            successCount++
            binding.dataProgress.setStatus(BackupProgressView.Status.Failed)
        }).addTo(disposable)
    }


    private fun runOnIo() = if (apkInfo == null) {
        Single.error(Exception())
    } else {
        Single.just(apkInfo!!).subscribeOn(Schedulers.io())
    }
}