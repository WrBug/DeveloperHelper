package com.wrbug.developerhelper.ui.activity.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseVMActivity
import com.wrbug.developerhelper.basecommon.obtainViewModel
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.databinding.ActivityMainBinding
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.shell.ShellManager
import com.wrbug.developerhelper.ui.activity.main.viewmodel.MainViewModel
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
        initListener()
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


    inner class Presenter {
        fun onAccessibilityClick() {
            ShellManager.getTopActivity()
            if (!accessibilitySettingView.checked) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
}
