package com.wrbug.developerhelper.base

import android.os.Handler
import android.os.Looper
import kotlin.concurrent.thread

private val handler by lazy {
    Handler(Looper.getMainLooper())
}

fun doAsync(callback: () -> Unit) {
    thread {
        callback()
    }
}

fun uiThread(callback: () -> Unit) {
    handler.post {
        callback()
    }
}