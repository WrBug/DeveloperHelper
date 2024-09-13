package com.wrbug.developerhelper.ui.widget.flexibletoast

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.databinding.LayoutToastFlexibleBinding

class FlexibleToast private constructor(private val mContext: Context) {

    companion object {
        const val GRAVITY_BOTTOM = 0
        const val GRAVITY_CENTER = 1
        const val GRAVITY_TOP = 2
        const val TOAST_SHORT = 0
        const val TOAST_LONG = 1
        private var instance: FlexibleToast? = null
        private fun getInstance(context: Context): FlexibleToast {
            if (instance == null) {
                instance = FlexibleToast(context.applicationContext)
            }
            return instance as FlexibleToast

        }

        fun toastShow(context: Context, msg: String) {
            val toast = getInstance(context)
            val builder = Builder(context).setGravity(GRAVITY_BOTTOM)
            builder.setSecondText(msg)
            if (Looper.myLooper() != Looper.getMainLooper()) {
                Handler(Looper.getMainLooper()).post { toast.toastShow(builder) }
            } else {
                toast.toastShow(builder)
            }
        }
    }

    private val flexibleToast: Toast by lazy {
        Toast(mContext)
    }

    fun toastShow(builder: Builder) {
        when {
            builder.mGravity == GRAVITY_CENTER -> flexibleToast.setGravity(
                Gravity.CENTER or Gravity.CENTER_VERTICAL,
                0,
                0
            )

            builder.mGravity == GRAVITY_TOP -> flexibleToast.setGravity(
                Gravity.TOP or Gravity.CENTER_VERTICAL,
                0,
                UiUtils.dp2px(mContext, 20F)
            )

            else -> flexibleToast.setGravity(
                Gravity.BOTTOM or Gravity.CENTER_VERTICAL,
                0,
                UiUtils.dp2px(mContext, 20F)
            )
        }
        if (builder.mDuration == TOAST_LONG) {
            flexibleToast.duration = Toast.LENGTH_LONG
        } else {
            flexibleToast.duration = Toast.LENGTH_SHORT
        }
        if (builder.hasCustomerView && builder.mCustomerView != null) {
            flexibleToast.view = builder.mCustomerView
        } else {
            flexibleToast.view = builder.binding.root
        }
        flexibleToast.show()
    }

    /**
     * 控制Toast的显示样式
     */
    class Builder(context: Context) {
        val binding = LayoutToastFlexibleBinding.inflate(LayoutInflater.from(context))
        var mCustomerView: View? = null

        var mDuration = Toast.LENGTH_SHORT// 0 short, 1 long
        var mGravity = 0
        var hasCustomerView = false // 是否使用自定义layout


        fun setImageResource(resId: Int): Builder {
            binding.imgIv.setImageResource(resId)
            binding.imgIv.visibility = View.VISIBLE
            binding.firstDividerView.visibility = View.VISIBLE
            return this
        }

        fun setFirstText(firstText: String): Builder {
            binding.firstTv.text = firstText
            binding.firstTv.visibility = View.VISIBLE
            binding.secondDividerView.visibility = View.VISIBLE
            return this
        }

        fun setSecondText(secondText: String): Builder {
            binding.secondTv.text = secondText
            binding.secondTv.visibility = View.VISIBLE
            return this
        }

        fun setDuration(duration: Int): Builder {
            this.mDuration = duration
            return this
        }

        fun setGravity(gravity: Int): Builder {
            this.mGravity = gravity
            return this
        }

        /**
         * 为Toast指定自定义的layout，此时上面对ImageView和TextView的设置失效。
         * @param customerView
         * @return
         */
        fun setCustomerView(customerView: View): Builder {
            this.mCustomerView = customerView
            this.hasCustomerView = true
            return this
        }
    }

}
