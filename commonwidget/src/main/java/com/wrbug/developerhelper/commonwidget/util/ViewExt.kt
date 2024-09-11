package com.wrbug.developerhelper.commonwidget.util

import android.os.SystemClock
import android.view.View
import android.widget.Toast
import com.wrbug.developerhelper.commonutil.ShellUtils
import com.wrbug.developerhelper.commonwidget.R
import com.wrbug.developerhelper.mmkv.ConfigKv
import com.wrbug.developerhelper.mmkv.manager.MMKVManager

private val configKv by lazy {
    MMKVManager.get(ConfigKv::class.java)
}

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


fun View?.setOnRootCheckClickListener(clickListener: (View) -> Unit) {
    setOnDoubleCheckClickListener {
        if (!configKv.isOpenRoot()) {
            Toast.makeText(it.context, R.string.open_root_notice, Toast.LENGTH_SHORT).show()
            return@setOnDoubleCheckClickListener
        }
        clickListener(it)
    }
}


inline var View.visible: Boolean
    set(value) {
        visibility = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    get() = visibility == View.VISIBLE


inline var View.inVisible: Boolean
    set(value) {
        visibility = if (value) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
    }
    get() = visibility == View.INVISIBLE