package com.wrbug.developerhelper.ui.activity.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.wrbug.developerhelper.BuildConfig
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseVMActivity
import com.wrbug.developerhelper.basecommon.obtainViewModel
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.commonutil.ClipboardUtils
import com.wrbug.developerhelper.commonutil.shell.Callback
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.commonutil.toInt
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.databinding.ActivityMainBinding
import com.wrbug.developerhelper.model.entity.VersionInfo
import com.wrbug.developerhelper.service.AccessibilityManager
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.activity.main.viewmodel.MainViewModel
import com.wrbug.developerhelper.ui.activity.xposed.xposedsetting.XposedSettingActivity
import com.wrbug.developerhelper.ui.widget.settingitemview.SettingItemView
import com.wrbug.developerhelper.util.DeviceUtils
import com.wrbug.developerhelper.util.UpdateUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseVMActivity<MainViewModel>() {


    lateinit var binding: ActivityMainBinding
    lateinit var xposedSettingView: SettingItemView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (DeviceUtils.isFloatWindowOpened()) {
            FloatWindowService.start(this)
            "".toInt()
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.presenter = Presenter()
        xposedSettingView = binding.xposedSettingView
        binding.mainVm = vm
        setupActionBar(R.id.toolbar) {

        }
        ShellManager.openAccessibilityService()
        initListener()
        val filter = IntentFilter(ReceiverConstant.ACTION_ACCESSIBILITY_SERVICE_STATUS_CHANGED)
        registerReceiver(receiver, filter)
    }

    private fun initListener() {
        floatWindowSettingView.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if (isChecked && DeviceUtils.isFloatWindowOpened()) {
                FloatWindowService.start(this)
            } else {
                FloatWindowService.stop(this)
            }
        })
        rootSettingView.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, _ ->

        })
        xposedSettingView.setOnClickListener {
            if (xposedSettingView.isChecked().not()) {
                showSnack(getString(R.string.open_xposed_first))
                return@setOnClickListener
            }
            startActivity(Intent(this, XposedSettingActivity::class.java))
        }
    }

    override fun getViewModel(): MainViewModel {
        return obtainViewModel(MainViewModel::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    inner class Presenter {
        fun onAccessibilityClick() {
            if (!accessibilitySettingView.checked) {
                AccessibilityManager.startAccessibilitySetting(context)
            } else {
                showSnack(getString(R.string.accessibility_service_opened))
            }
        }

        fun onFloatWindowClick() {
            if (!floatWindowSettingView.checked) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                    0
                )
            } else {
                showSnack(getString(R.string.float_window_opened))
            }
        }

        fun onRootClick() {
            vm.toggleRootPermission()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (it.itemId) {
                R.id.about_menu -> {
                    showAboutDialog()
                }
                R.id.exit_menu -> {
                    showExitMenuDialog()
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showExitMenuDialog() = AlertDialog.Builder(this)
        .setTitle(R.string.notice)
        .setMessage(getString(R.string.exit_content))
        .setPositiveButton(getString(R.string.ok)) { _, _ ->
            FloatWindowService.stop(this)
            finish()
        }
        .setNegativeButton(getString(R.string.cancel), null)
        .create()
        .show()

    private fun showAboutDialog() = AlertDialog.Builder(this)
        .setTitle(R.string.about)
        .setMessage(getString(R.string.about_content))
        .setPositiveButton(getString(R.string.copy_group_number)) { _, _ ->
            ClipboardUtils.saveClipboardText(this, "627962572")
            showSnack(R.string.copy_success)
        }
        .setNeutralButton(getString(R.string.check_update)) { _, _ ->
            checkUpdate(true)
        }
        .create().show()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    private fun checkUpdate(showSnack: Boolean = false) {
        if (showSnack) {
            showSnack(getString(R.string.checking_update))
        }
        UpdateUtils.checkUpdate(object : Callback<VersionInfo> {
            override fun onSuccess(data: VersionInfo) {
                if (BuildConfig.VERSION_NAME == data.versionName) {
                    showSnack(getString(R.string.no_new_version))
                    return
                }
                showUpdateDialog(data)
            }

            override fun onFailed(msg: String) {
                if (showSnack) {
                    showSnack(getString(R.string.check_update_failed))
                }
            }
        })
    }

    private fun showUpdateDialog(data: VersionInfo) = AlertDialog.Builder(this)
        .setTitle(getString(R.string.find_new_version))
        .setMessage("版本号:${data.versionName}\n更新时间：${data.updateDate}\n大小：${data.size}\n版本说明：\n${data.feature}")
        .setPositiveButton(getString(R.string.download)) { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.parse(data.downloadUrl)
            intent.data = uri
            startActivity(intent)
        }
        .create().show()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.run {
                if (action == ReceiverConstant.ACTION_ACCESSIBILITY_SERVICE_STATUS_CHANGED) {
                    accessibilitySettingView.checked = intent.getBooleanExtra("status", false)
                }
            }
        }

    }
}
