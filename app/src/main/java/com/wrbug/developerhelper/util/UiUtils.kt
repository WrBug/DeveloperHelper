package com.wrbug.developerhelper.util

import android.content.Context
import android.util.TypedValue
import com.wrbug.developerhelper.basecommon.BaseApp

object UiUtils {

    fun dp2px(context: Context = BaseApp.instance, dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal,
            context.resources?.displayMetrics
        ).toInt()
    }

    fun sp2px(context: Context = BaseApp.instance,spVal: Float): Int {
        return TypedValue.applyDimension(2, spVal, context.resources?.displayMetrics).toInt()
    }

    fun px2dp(context: Context = BaseApp.instance,pxVal: Float): Float {
        val scale = context.resources?.displayMetrics?.density!!
        return pxVal / scale
    }

    fun px2sp(context: Context = BaseApp.instance,pxVal: Float): Float {
        return pxVal / context.resources?.displayMetrics?.scaledDensity!!
    }

    /**
     * 获取屏幕高度
     */
    fun getDeviceHeight(context: Context = BaseApp.instance): Int {
        val dm = context.resources?.displayMetrics
        return dm?.heightPixels ?: 0
    }

    /**
     * 获取屏幕宽度
     */
    fun getDeviceWidth(context: Context = BaseApp.instance): Int {
        val dm = context.resources?.displayMetrics
        return dm?.widthPixels ?: 0
    }

    /**
     * 获取状态栏的高度
     */
    fun getStatusHeight(context: Context = BaseApp.instance): Int {
        var result: Int? = 10
        val resourceId =
            context.resources?.getIdentifier("status_bar_height", "dimen", "android") ?: 0
        if (resourceId > 0) {
            result = context.resources?.getDimensionPixelOffset(resourceId)
        }
        return result ?: 0
    }
}