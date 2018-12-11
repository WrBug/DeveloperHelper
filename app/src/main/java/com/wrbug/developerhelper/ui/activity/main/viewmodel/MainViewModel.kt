package com.wrbug.developerhelper.ui.activity.main.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.databinding.BaseObservable
import android.databinding.ObservableField
import com.wrbug.developerhelper.basecommon.BaseViewModel
import com.wrbug.developerhelper.model.mmkv.ConfigKv
import com.wrbug.developerhelper.model.mmkv.manager.MMKVManager
import com.wrbug.developerhelper.service.DeveloperHelperAccessibilityService
import com.wrbug.developerhelper.util.DeviceUtils
import javax.inject.Inject

class MainViewModel @Inject constructor(): BaseViewModel() {
    val openAccessibility = ObservableField<Boolean>(false)
    val openFloatWindow = ObservableField<Boolean>(false)
    val openRoot = ObservableField<Boolean>(false)
    val openXposed = ObservableField<Boolean>(false)
    val configKv: ConfigKv = MMKVManager.get(ConfigKv::class.java)
    fun checkStatus() {
        openAccessibility.set(DeveloperHelperAccessibilityService.serviceRunning)
        openFloatWindow.set(DeviceUtils.isFloatWindowOpened())
    }
}
