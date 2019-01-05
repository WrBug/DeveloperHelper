package com.wrbug.developerhelper.commonutil.shell

interface Callback<T> {
    fun onSuccess(data: T)
    fun onFailed(msg: String) {}
}