package com.wrbug.developerhelper.ui.activity.appbackup

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.BaseActivity
import com.wrbug.developerhelper.base.ExtraKey
import com.wrbug.developerhelper.base.registerReceiverComp
import com.wrbug.developerhelper.base.sendBroadcastComp
import com.wrbug.developerhelper.base.versionCodeLong
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.getParcelableCompat
import com.wrbug.developerhelper.commonutil.getSerializableCompat
import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.databinding.ActivityAppRecoverBinding
import com.wrbug.developerhelper.model.entity.BackupAppItemInfo
import com.wrbug.developerhelper.ui.activity.appbackup.entity.RecoverTimeLineItem
import com.wrbug.developerhelper.ui.activity.appbackup.worker.AppRecoverWorker
import com.wrbug.developerhelper.ui.activity.appbackup.worker.AppRecoverWorkerData
import com.wrbug.developerhelper.ui.adapter.ExMultiTypeAdapter
import com.wrbug.developerhelper.util.getString
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener
import org.jetbrains.anko.toast

class AppRecoverActivity : BaseActivity() {

    companion object {
        fun start(context: Context, appName: String, backupAppItemInfo: BackupAppItemInfo) {
            context.startActivity(Intent(context, AppRecoverActivity::class.java).apply {
                putExtra(ExtraKey.KEY_1, backupAppItemInfo as Parcelable)
                putExtra(ExtraKey.KEY_2, appName)
            })
        }

        fun startForResult(
            activity: Activity,
            requestCode: Int,
            appName: String,
            backupAppItemInfo: BackupAppItemInfo
        ) {
            activity.startActivityForResult(Intent(activity, AppRecoverActivity::class.java).apply {
                putExtra(ExtraKey.KEY_1, backupAppItemInfo as Parcelable)
                putExtra(ExtraKey.KEY_2, appName)
            }, requestCode)
        }
    }

    private val backupAppItemInfo: BackupAppItemInfo? by lazy {
        intent?.getParcelableCompat(ExtraKey.KEY_1)
    }
    private val appName: String by lazy {
        intent?.getStringExtra(ExtraKey.KEY_2).orEmpty()
    }
    private val apkInfo by lazy {
        AppInfoManager.getAppByPackageName(backupAppItemInfo?.packageName.orEmpty())
    }
    private val adapter by ExMultiTypeAdapter.get()

    private val binding by lazy {
        ActivityAppRecoverBinding.inflate(layoutInflater)
    }
    private val receiver by lazy {
        Receiver()
    }
    private val timeLineList: ArrayList<RecoverTimeLineItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initListener()
        loadData()
    }

    private fun loadData() {
        if (apkInfo == null && backupAppItemInfo?.backupApk == true) {
            binding.cbApk.isChecked = true
            binding.cbApk.isEnabled = false
        }
    }

    private fun initListener() {
        binding.viewCbMask.setOnDoubleCheckClickListener { }
        binding.cbApk.setOnCheckedChangeListener { _, _ ->
            updateItemList()
        }
        binding.cbData.setOnCheckedChangeListener { _, _ ->
            updateItemList()
        }
        binding.cbAndroidData.setOnCheckedChangeListener { _, _ ->
            updateItemList()
        }
        binding.btnRecover.setOnDoubleCheckClickListener {
            if (it.tag == true) {
                toast(getString(R.string.recover_success_launch_app, appName))
                setResult(RESULT_OK)
                finish()
                sendBroadcastComp(ReceiverConstant.ACTION_DELAY_START_APP) {
                    it.putExtra(ExtraKey.PACKAGE_NAME, backupAppItemInfo?.packageName)
                }
                return@setOnDoubleCheckClickListener
            }
            updateItemList()
            apkVersionCheck()
        }
        val intentFilter = IntentFilter(AppRecoverWorker.ACTION_STEP_STATUS)
        intentFilter.addAction(AppRecoverWorker.ACTION_SUCCESS)
        registerReceiverComp(receiver, intentFilter)
    }

    private fun apkVersionCheck() {
        val backupVersionCode = backupAppItemInfo?.versionCode ?: return
        val backupVersionName = backupAppItemInfo?.versionName ?: return
        val appVersionCode = apkInfo?.packageInfo?.versionCodeLong ?: return
        val appVersionName = apkInfo?.packageInfo?.versionName ?: return
        if (!binding.cbApk.isChecked && apkInfo != null && backupVersionCode != appVersionCode) {
            showDialog(
                R.string.notice,
                R.string.apk_version_backup_version_not_same_notice.getString(
                    "$appVersionName($appVersionCode)", "$backupVersionName($backupVersionCode)",
                ),
                R.string.ok,
                R.string.cancel,
                {
                    systemAppCheck()
                    dismiss()
                },
                {
                    dismiss()
                })
            return
        }
        systemAppCheck()
    }

    private fun systemAppCheck() {
        startRecover()
    }

    private fun startRecover() {
        val constraints = Constraints.Builder().build()
        val data = Data.Builder().putString(
            AppRecoverWorker.DATA, AppRecoverWorkerData(
                appItemInfo = backupAppItemInfo,
                appName = appName,
                recoverApk = binding.cbApk.isChecked,
                recoverData = binding.cbData.isChecked,
                recoverAndroidData = binding.cbAndroidData.isChecked
            ).toJson()
        ).build()
        WorkManager.getInstance(this).beginUniqueWork(
            AppRecoverWorker.TAG,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<AppRecoverWorker>().addTag(AppRecoverWorker.TAG)
                .setInputData(data).setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST).build()
        ).enqueue()
        binding.viewCbMask.isVisible = true
        binding.btnRecover.isEnabled = false
        binding.btnRecover.text = "正在恢复数据..."
    }

    private fun updateItemList() {
        timeLineList.clear()
        if (!binding.cbApk.isChecked && !binding.cbData.isChecked && !binding.cbAndroidData.isChecked) {
            binding.btnRecover.isEnabled = false
            adapter.loadData(timeLineList)
            return
        }
        binding.btnRecover.isEnabled = true
        timeLineList.add(RecoverTimeLineItem("初始化准备", 1))
        if (binding.cbApk.isChecked) {
            timeLineList.add(
                RecoverTimeLineItem(
                    R.string.time_line_recover_title.getString(R.string.item_backup_apk.getString()),
                    0
                )
            )
        }
        if (binding.cbData.isChecked) {
            timeLineList.add(
                RecoverTimeLineItem(
                    R.string.time_line_recover_title.getString(R.string.item_backup_data.getString()),
                    0
                )
            )
        }
        if (binding.cbAndroidData.isChecked) {
            timeLineList.add(
                RecoverTimeLineItem(
                    R.string.time_line_recover_title.getString(R.string.item_backup_android_data.getString()),
                    0
                )
            )
        }
        timeLineList.last().lineType = 2
        adapter.loadData(timeLineList)
    }

    private fun initView() {
        binding.cbApk.isEnabled = backupAppItemInfo?.backupApk == true
        binding.cbData.isEnabled = backupAppItemInfo?.backupData == true
        binding.cbAndroidData.isEnabled = backupAppItemInfo?.backupAndroidData == true
        binding.rvTimeLine.layoutManager = LinearLayoutManager(this)
        binding.rvTimeLine.adapter = adapter
        adapter.register(RecoverTimeLineDelegate())
        adapter.loadData(timeLineList)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                AppRecoverWorker.ACTION_STEP_STATUS -> {
                    val (step, status) = intent.getSerializableCompat<Pair<Int, Int>>(ExtraKey.KEY_1)
                        ?: return
                    timeLineList[step].status = RecoverTimeLineItem.Status.get(status)
                    adapter.notifyItemChanged(step)
                }

                AppRecoverWorker.ACTION_SUCCESS -> {
                    binding.btnRecover.isEnabled = true
                    binding.btnRecover.tag = true
                    binding.btnRecover.text = "打开 $appName"
                }

                AppRecoverWorker.ACTION_FAILED -> {
                    binding.btnRecover.isEnabled = true
                    binding.viewCbMask.isVisible = false
                }

                else -> {

                }
            }
        }

    }
}