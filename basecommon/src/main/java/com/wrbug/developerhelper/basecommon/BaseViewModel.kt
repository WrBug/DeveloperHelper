package com.wrbug.developerhelper.basecommon

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    val snackBar = MutableLiveData<String>()
}