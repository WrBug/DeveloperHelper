package com.wrbug.developerhelper.commonwidget.util

import android.os.SystemClock
import android.view.View
import com.wrbug.developerhelper.commonwidget.R

fun View?.setOnDoubleCheckClickListener(duration: Long = 800, clickListener: (View) -> Unit) {
    this?.setOnClickListener {
        val time = SystemClock.elapsedRealtime()
        val lastTime = (it.getTag(R.id.double_check_click) as? Long) ?: 0
        if (time - lastTime > duration) {
            it.setTag(R.id.double_check_click, time)
            clickListener(it)
        }
    }
}