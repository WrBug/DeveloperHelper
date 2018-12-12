package com.wrbug.developerhelper.ui.activity.main

import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.basecommon.BaseVMActivity
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.databinding.ActivityMainBinding
import com.wrbug.developerhelper.model.mmkv.ConfigKv
import com.wrbug.developerhelper.model.mmkv.manager.MMKVManager
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.activity.main.viewmodel.MainViewModel
import com.wrbug.developerhelper.util.DeviceUtils


class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FloatWindowService.start(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.presenter = Presenter()
//        binding.mainVm = vm
        setupActionBar(R.id.toolbar) {

        }
    }
//    override fun getViewModel(): MainViewModel {
//        return obtainViewModel(MainViewModel::class.java)
//    }
    override fun onResume() {
        super.onResume()
//        vm.checkStatus()
        if (DeviceUtils.isFloatWindowOpened()) {
            FloatWindowService.start(this)
        }
    }


    inner class Presenter {
        fun onAccessibilityClick() {
            if (!binding.accessibilitySettingView.checked) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        }

        fun onFloatWindowClick() {
            if (!binding.floatWindowSettingView.checked) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                    0
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }
}
