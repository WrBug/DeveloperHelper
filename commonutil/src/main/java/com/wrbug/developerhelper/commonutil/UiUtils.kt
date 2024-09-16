package com.wrbug.developerhelper.commonutil

import android.app.Dialog
import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.fragment.app.Fragment

fun Context.dp2px(dpVal: Float): Int = UiUtils.dp2px(this, dpVal)
fun Fragment.dp2px(dpVal: Float): Int = UiUtils.dp2px(activity!!, dpVal)
fun Dialog.dp2px(dpVal: Float): Int = UiUtils.dp2px(context, dpVal)
fun View.dp2px(dpVal: Float): Int = UiUtils.dp2px(context, dpVal)
fun Float.dpInt(context: Context) = UiUtils.dp2px(context, this)
fun Int.dpInt(context: Context = CommonUtils.application) = UiUtils.dp2px(context, toFloat())

object UiUtils {
    private var statusBarHeight: Int = -1
    fun dp2px(context: Context = CommonUtils.application, dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpVal, context.resources?.displayMetrics
        ).toInt()
    }

    fun px2dp(context: Context = CommonUtils.application, pxVal: Float): Float {
        val scale = context.resources?.displayMetrics?.density!!
        return pxVal / scale
    }

    fun px2sp(context: Context = CommonUtils.application, pxVal: Float): Float {
        return pxVal / context.resources?.displayMetrics?.scaledDensity!!
    }

    /**
     * 获取屏幕高度
     */
    fun getDeviceHeight(context: Context = CommonUtils.application): Int {
        val dm = context.resources?.displayMetrics
        return dm?.heightPixels ?: 0
    }

    /**
     * 获取屏幕宽度
     */
    fun getDeviceWidth(context: Context = CommonUtils.application): Int {
        val dm = context.resources?.displayMetrics
        return dm?.widthPixels ?: 0
    }

    /**
     * 获取状态栏的高度
     */
    fun getStatusHeight(context: Context = CommonUtils.application): Int {
        if (statusBarHeight != -1) {
            return statusBarHeight
        }
        var result: Int? = 10
        val resourceId =
            context.resources?.getIdentifier("status_bar_height", "dimen", "android") ?: 0
        if (resourceId > 0) {
            result = context.resources?.getDimensionPixelOffset(resourceId)
        }
        statusBarHeight = result ?: 0
        return statusBarHeight
    }
}