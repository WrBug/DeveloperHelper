package com.wrbug.developerhelper.util

import android.app.ActionBar.LayoutParams
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.wrbug.developerhelper.R
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

fun FrameLayout.startPageLoading() {
    val lottieView = LottieAnimationView(context)
    lottieView.id = R.id.lottie_loading
    lottieView.setAnimation(R.raw.lottie_page_loading)
    lottieView.playAnimation()
    lottieView.repeatCount = LottieDrawable.INFINITE
    removeAllViews()
    isVisible = true
    addView(
        lottieView,
        FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    )
}

fun FrameLayout.stopPageLoading() {
    val lottieView: LottieAnimationView? = findViewById(R.id.lottie_loading)
    lottieView?.cancelAnimation()
    removeAllViews()
    isVisible = false
}


fun ImageView.loadImage(url: Any?, default: Int? = null) {
    Glide.with(this).load(url).apply {
        if (default != null) {
            error(default)
        }
    }.into(this)
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