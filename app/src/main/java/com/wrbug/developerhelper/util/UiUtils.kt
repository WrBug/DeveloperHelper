package com.wrbug.developerhelper.util

import android.content.Context
import android.util.TypedValue
import com.wrbug.developerhelper.basecommon.BaseApp

object UiUtils {

    fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpVal,
            BaseApp.instance.resources?.displayMetrics
        ).toInt()
    }

    fun sp2px(spVal: Float): Int {
        return TypedValue.applyDimension(2, spVal, BaseApp.instance.resources?.displayMetrics).toInt()
    }

    fun px2dp(pxVal: Float): Float {
        val scale = BaseApp.instance.resources?.displayMetrics?.density!!
        return pxVal / scale
    }

    fun px2sp(pxVal: Float): Float {
        return pxVal / BaseApp.instance.resources?.displayMetrics?.scaledDensity!!
    }

    /**
     * 获取屏幕高度
     */
    fun getDeviceHeight(): Int {
        val dm = BaseApp.instance.resources?.displayMetrics
        return dm?.heightPixels ?: 0
    }

    /**
     * 获取屏幕宽度
     */
    fun getDeviceWidth(): Int {
        val dm = BaseApp.instance?.resources?.displayMetrics
        return dm?.widthPixels ?: 0
    }

    /**
     * 获取状态栏的高度
     */
    fun getStatusHeight(): Int {
        var result: Int? = 10
        val resourceId =
            BaseApp.instance.resources?.getIdentifier("status_bar_height", "dimen", "android") ?: 0
        if (resourceId > 0) {
            result = BaseApp.instance.resources?.getDimensionPixelOffset(resourceId)
        }
        return result ?: 0
    }
}