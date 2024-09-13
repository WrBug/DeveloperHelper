package com.wrbug.developerhelper.base

import android.os.Bundle

abstract class BaseVMActivity<VM : BaseViewModel> : BaseActivity() {
    protected lateinit var vm: VM
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = getViewModel()
        lifecycle.addObserver(vm)
        initObserve()
    }

    private fun initObserve() {
        vm.snackBar.observeForever {
            showSnack(it)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(vm)
    }

    protected abstract fun getViewModel(): VM

}
