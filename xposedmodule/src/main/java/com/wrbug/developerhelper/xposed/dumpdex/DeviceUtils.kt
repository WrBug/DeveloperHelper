package com.wrbug.developerhelper.xposed.dumpdex

import android.os.Build

/**
 * DeviceUtils
 *
 * @author WrBug
 * @since 2018/4/8
 */
object DeviceUtils {
    private var sdkInit: Int = 0

    val isNougat: Boolean
        get() = sdkInit == 24 || sdkInit == 25

    val isOreo: Boolean
        get() = sdkInit == 26 || sdkInit == 27

    val isPie: Boolean
        get() = sdkInit == 28
    val isMarshmallow: Boolean
        get() = sdkInit == 23

    init {
        sdkInit = Build.VERSION.SDK_INT
    }

    fun supportNativeHook(): Boolean {
        return isNougat || isMarshmallow || isOreo || isPie
    }
}
