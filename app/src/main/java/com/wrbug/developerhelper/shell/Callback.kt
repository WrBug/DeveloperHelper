package com.wrbug.developerhelper.shell

interface Callback<T> {
    fun onSuccess(data: T)
    fun onFailed(msg: String) {}
}