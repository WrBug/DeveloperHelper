package com.wrbug.developerhelper.ui.activity.appbackup

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.BaseActivity
import com.wrbug.developerhelper.base.ExtraKey
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.GlobalEvent
import com.wrbug.developerhelper.commonutil.addTo
import com.wrbug.developerhelper.commonutil.dpInt
import com.wrbug.developerhelper.databinding.ActivityAppBackupDetailBinding
import com.wrbug.developerhelper.model.entity.BackupAppItemInfo
import com.wrbug.developerhelper.ui.adapter.ExMultiTypeAdapter
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import com.wrbug.developerhelper.util.BackupUtils
import com.yanzhenjie.recyclerview.SwipeMenuItem

class AppBackupDetailActivity : BaseActivity() {

    companion object {
        private const val REQUEST_CODE = 100
        fun start(context: Context, appName: String, packageName: String, fromAppSetting: Boolean) {
            context.startActivity(Intent(context, AppBackupDetailActivity::class.java).apply {
                putExtra(ExtraKey.PACKAGE_NAME, packageName)
                putExtra(ExtraKey.KEY_1, appName)
                putExtra(ExtraKey.KEY_2, fromAppSetting)
            })
        }
    }


    private val adapter by ExMultiTypeAdapter.get()

    private val pkgName by lazy {
        intent?.getStringExtra(ExtraKey.PACKAGE_NAME).orEmpty()
    }
    private val appName by lazy {
        intent?.getStringExtra(ExtraKey.KEY_1).orEmpty()
    }
    private val fromAppSetting by lazy {
        intent?.getBooleanExtra(ExtraKey.KEY_2, false) ?: false
    }
    private val binding by lazy {
        ActivityAppBackupDetailBinding.inflate(layoutInflater)
    }
    private var listCache = arrayListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        loadData()
    }

    private fun loadData() {
        adapter.showLoading()
        BackupUtils.getBackupAppInfo(pkgName).subscribe({
            val list = it.backupMap.values.sortedByDescending { it.time }
            listCache.clear()
            listCache.addAll(list)
            setupList()
        }, {
            adapter.showEmpty()
        }).addTo(disposable)

    }

    private fun setupList() {
        if (listCache.isEmpty()) {
            adapter.showEmpty()
        } else {
            adapter.loadData(listCache)
        }
    }

    private fun initView() {
        binding.appBar.setSubTitle(appName)
        binding.rvAppBackupList.layoutManager = LinearLayoutManager(this)
        binding.rvAppBackupList.addItemDecoration(SpaceItemDecoration.standard)
        adapter.register(BackupDetailDelegate(appName))
        binding.rvAppBackupList.setSwipeMenuCreator { _, rightMenu, _ ->
            rightMenu.addMenuItem(SwipeMenuItem(this).apply {
                text = getString(R.string.item_swipe_menu_restore)
                width = 56.dpInt()
                height = LayoutParams.MATCH_PARENT
                setTextColorResource(R.color.material_text_color_white_text)
                setBackgroundColorResource(R.color.colorAccent)
            })
            rightMenu.addMenuItem(SwipeMenuItem(this).apply {
                text = getString(R.string.item_swipe_menu_delete)
                width = 56.dpInt()
                height = LayoutParams.MATCH_PARENT
                setTextColorResource(R.color.material_text_color_white_text)
                setBackgroundColorResource(R.color.material_color_red_600)
            })
        }
        binding.rvAppBackupList.setOnItemMenuClickListener { menuBridge, adapterPosition ->
            menuBridge.closeMenu()
            when (menuBridge.position) {
                0 -> {
                    recover(adapter.items[adapterPosition] as? BackupAppItemInfo)
                }

                1 -> {
                    showDialog(
                        R.string.notice,
                        R.string.delete_backup_notice,
                        R.string.ok,
                        R.string.cancel,
                        {
                            deleteBackup(adapter.items[adapterPosition] as? BackupAppItemInfo)
                            dismiss()
                        },
                        {
                            dismiss()
                        })
                }

                else -> {}
            }
        }
        binding.rvAppBackupList.adapter = adapter
    }

    private fun recover(backupAppItemInfo: BackupAppItemInfo?) {
        backupAppItemInfo ?: return
        if (!AppInfoManager.isInstalled(backupAppItemInfo.packageName) && !backupAppItemInfo.backupApk) {
            showSnack(getString(R.string.not_installed_and_backup_apk_notice))
            return
        }
        if (fromAppSetting) {
            AppRecoverActivity.startForResult(this, REQUEST_CODE, appName, backupAppItemInfo)
        } else {
            AppRecoverActivity.start(this, appName, backupAppItemInfo)
        }
    }

    private fun deleteBackup(backupAppItemInfo: BackupAppItemInfo?) {
        backupAppItemInfo ?: return
        BackupUtils.deleteBackupItem(backupAppItemInfo.packageName, backupAppItemInfo.backupFile)
            .subscribe({
                loadData()
            }, {
                showSnack(getString(R.string.delete_backup_failed_retry))
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                GlobalEvent.postEvent(GlobalEvent.Action.CloseAll)
                finish()
            }
        }
    }
}