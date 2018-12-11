package com.wrbug.developerhelper.basecommon

import android.os.Bundle

abstract class BaseVMActivity<VM : BaseViewModel> : BaseActivity() {
    protected lateinit var vm: VM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = getViewModel()
    }

    protected abstract fun getViewModel(): VM

}
