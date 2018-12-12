package com.wrbug.developerhelper.basecommon

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class BaseViewModel : ViewModel() {
    val snackBar = MutableLiveData<String>()
}