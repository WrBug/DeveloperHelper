package com.wrbug.developerhelper.base

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel(), LifecycleObserver {
    val snackBar = MutableLiveData<String>()
    val application = BaseApp.instance

    fun showSnack(msg: String) {
        snackBar.postValue(msg)
    }

    fun showSnack(id: Int) {
        showSnack(application.resources.getString(id))
    }
}