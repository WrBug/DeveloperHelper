package com.wrbug.developerhelper.ui.activity.main.viewmodel

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseViewModel
import com.wrbug.developerhelper.mmkv.ConfigKv
import com.wrbug.developerhelper.mmkv.manager.MMKVManager
import com.wrbug.developerhelper.service.DeveloperHelperAccessibilityService
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.util.DeviceUtils
import javax.inject.Inject

class MainViewModel @Inject constructor() : BaseViewModel() {
    val openAccessibility = ObservableBoolean()
    val openFloatWindow = ObservableBoolean()
    val openRoot = ObservableBoolean()
    private val configKv: ConfigKv = MMKVManager.get(ConfigKv::class.java)
    fun checkStatus() {
        openAccessibility.set(DeveloperHelperAccessibilityService.serviceRunning)
        openFloatWindow.set(DeviceUtils.isFloatWindowOpened())
        if (configKv.isOpenRoot()) {
            if (DeviceUtils.isRoot()) {
                openRoot.set(true)
            } else {
                openRoot.set(false)
                configKv.setOpenRoot(false)
            }
        }
        if (DeviceUtils.isFloatWindowOpened()) {
            FloatWindowService.start(application)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        checkStatus()
    }

    fun toggleRootPermission() {
        if (!DeviceUtils.isRoot()) {
            showSnack(R.string.devices_is_not_root)
            return
        }
        openRoot.set(!openRoot.get())
        configKv.setOpenRoot(openRoot.get())
    }
}
