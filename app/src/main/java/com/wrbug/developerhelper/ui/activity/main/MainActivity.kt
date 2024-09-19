package com.wrbug.developerhelper.ui.activity.main

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.base.BaseActivity
import com.wrbug.developerhelper.base.registerReceiverComp
import com.wrbug.developerhelper.base.requestStoragePermission
import com.wrbug.developerhelper.base.setupActionBar
import com.wrbug.developerhelper.commonutil.ClipboardUtils
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.databinding.ActivityMainBinding
import com.wrbug.developerhelper.mmkv.ConfigKv
import com.wrbug.developerhelper.mmkv.manager.MMKVManager
import com.wrbug.developerhelper.model.entity.VersionInfo
import com.wrbug.developerhelper.service.AccessibilityManager
import com.wrbug.developerhelper.service.DeveloperHelperAccessibilityService
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.activity.appbackup.BackupAppActivity
import com.wrbug.developerhelper.util.DeviceUtils

class MainActivity : BaseActivity() {
    private val configKv: ConfigKv = MMKVManager.get(ConfigKv::class.java)
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DeviceUtils.isFloatWindowOpened()) {
            FloatWindowService.start(this)
        }
        setContentView(binding.root)
        setupActionBar(R.id.toolbar)
        ShellManager.openAccessibilityService()
        initView()
        initListener()
        val filter = IntentFilter(ReceiverConstant.ACTION_ACCESSIBILITY_SERVICE_STATUS_CHANGED)
        registerReceiverComp(receiver, filter)
    }

    private fun initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.notificationSettingView.checkable = false
            binding.notificationSettingView.isVisible = true
            binding.notificationSettingView.checked =
                hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            binding.notificationSettingView.isVisible = false
        }
    }

    private fun initListener() {
        binding.floatWindowSettingView.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && DeviceUtils.isFloatWindowOpened()) {
                FloatWindowService.start(this)
            } else {
                FloatWindowService.stop(this)
            }
        }
        binding.backupAppSettingView.setOnDoubleCheckClickListener {
            requestStoragePermission {
                startActivity(Intent(this, BackupAppActivity::class.java))
            }
        }
        binding.accessibilitySettingView.setOnDoubleCheckClickListener {
            if (!binding.accessibilitySettingView.checked) {
                AccessibilityManager.startAccessibilitySetting(context)
            } else {
                showSnack(getString(R.string.accessibility_service_opened))
            }
        }
        binding.floatWindowSettingView.setOnDoubleCheckClickListener {
            if (!binding.floatWindowSettingView.checked) {
                startActivityForResult(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
                    ), 0
                )
            } else {
                showSnack(getString(R.string.float_window_opened))
            }
        }
        binding.rootSettingView.setOnDoubleCheckClickListener {
            if (!DeviceUtils.isRoot()) {
                showSnack(R.string.devices_is_not_root)
                return@setOnDoubleCheckClickListener
            }
            val isRootEnable = binding.rootSettingView.isChecked()
            binding.rootSettingView.checked = !isRootEnable
            configKv.setOpenRoot(!isRootEnable)
        }
        binding.notificationSettingView.setOnSwitcherClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermission(arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    object : PermissionCallback() {
                        override fun granted() {
                            binding.notificationSettingView.checked = true
                        }
                    })
            }
        }
    }


    override fun onResume() {
        super.onResume()
        checkStatus()
    }


    private fun checkStatus() {
        binding.accessibilitySettingView.checked =
            DeveloperHelperAccessibilityService.serviceRunning
        binding.floatWindowSettingView.checked = DeviceUtils.isFloatWindowOpened()
        if (configKv.isOpenRoot()) {
            if (DeviceUtils.isRoot()) {
                binding.rootSettingView.checked = true
            } else {
                binding.rootSettingView.checked = false
                configKv.setOpenRoot(false)
            }
        }
        if (DeviceUtils.isFloatWindowOpened()) {
            FloatWindowService.start(application)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about_menu -> {
                showAboutDialog()
            }

            R.id.exit_menu -> {
                showExitMenuDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showExitMenuDialog() = AlertDialog.Builder(this).setTitle(R.string.notice)
        .setMessage(getString(R.string.exit_content))
        .setPositiveButton(getString(R.string.ok)) { _, _ ->
            FloatWindowService.stop(this)
            finish()
        }.setNegativeButton(getString(R.string.cancel), null).create().show()

    private fun showAboutDialog() = AlertDialog.Builder(this).setTitle(R.string.about)
        .setMessage(getString(R.string.about_content))
        .setPositiveButton(getString(R.string.copy_group_number)) { _, _ ->
            ClipboardUtils.saveClipboardText(this, "627962572")
            showSnack(R.string.copy_success)
        }.setNeutralButton(getString(R.string.check_update)) { _, _ ->
            checkUpdate(true)
        }.create().show()

    private fun checkUpdate(showSnack: Boolean = false) {
//        if (showSnack) {
//            showSnack(getString(R.string.checking_update))
//        }
//        UpdateUtils.checkUpdate(object : Callback<VersionInfo> {
//            override fun onSuccess(data: VersionInfo) {
//                if (BuildConfig.VERSION_NAME == data.versionName) {
//                    showSnack(getString(R.string.no_new_version))
//                    return
//                }
//                showUpdateDialog(data)
//            }
//
//            override fun onFailed(msg: String) {
//                if (showSnack) {
//                    showSnack(getString(R.string.check_update_failed))
//                }
//            }
//        })
    }

    private fun showUpdateDialog(data: VersionInfo) =
        AlertDialog.Builder(this).setTitle(getString(R.string.find_new_version))
            .setMessage("版本号:${data.versionName}\n更新时间：${data.updateDate}\n大小：${data.size}\n版本说明：\n${data.feature}")
            .setPositiveButton(getString(R.string.download)) { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW)
                val uri = Uri.parse(data.downloadUrl)
                intent.data = uri
                startActivity(intent)
            }.create().show()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.run {
                if (action == ReceiverConstant.ACTION_ACCESSIBILITY_SERVICE_STATUS_CHANGED) {
                    binding.accessibilitySettingView.checked =
                        intent.getBooleanExtra("status", false)
                }
            }
        }

    }
}
