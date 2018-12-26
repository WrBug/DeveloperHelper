package com.wrbug.developerhelper.ui.activity.main

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseVMActivity
import com.wrbug.developerhelper.basecommon.obtainViewModel
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.databinding.ActivityMainBinding
import com.wrbug.developerhelper.service.AccessibilityManager
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.shell.ShellManager
import com.wrbug.developerhelper.ui.activity.main.viewmodel.MainViewModel
import com.wrbug.developerhelper.ui.activity.sharedpreferencesedit.SharedPreferenceEditActivity
import com.wrbug.developerhelper.util.ClipboardUtils
import com.wrbug.developerhelper.util.DeviceUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseVMActivity<MainViewModel>() {


    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FloatWindowService.start(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.presenter = Presenter()
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
        rootSettingView.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->

        })

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
                AccessibilityManager.startService(context)
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
        .create().show()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

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
