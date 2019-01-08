package com.wrbug.developerhelper.commonwidget.flexibletoast

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.commonwidget.R

class FlexibleToast(private val mContext: Context) {

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
            flexibleToast.view = builder.mDefaultView
        }
        flexibleToast.show()
    }

    /**
     * 控制Toast的显示样式
     */
    class Builder(context: Context) {
        val mDefaultView: View = LayoutInflater.from(context)
            .inflate(R.layout.layout_toast_flexible, null)
        var mCustomerView: View? = null
        private val mIvImage: ImageView
        private val mTvFirst: TextView
        private val mTvSecond: TextView

        private val dividerFirst: View
        private val dividerSecond: View

        var mDuration = Toast.LENGTH_SHORT// 0 short, 1 long
        var mGravity = 0
        var hasCustomerView = false // 是否使用自定义layout


        init {
            mIvImage = mDefaultView.findViewById(R.id.imgIv)
            mTvFirst = mDefaultView.findViewById(R.id.firstTv)
            mTvSecond = mDefaultView.findViewById(R.id.secondTv)
            dividerFirst = mDefaultView.findViewById(R.id.firstDividerView)
            dividerSecond = mDefaultView.findViewById(R.id.secondDividerView)
        }

        fun setImageResource(resId: Int): Builder {
            this.mIvImage.setImageResource(resId)
            this.mIvImage.visibility = View.VISIBLE
            this.dividerFirst.visibility = View.VISIBLE
            return this
        }

        fun setFirstText(firstText: String): Builder {
            this.mTvFirst.text = firstText
            this.mTvFirst.visibility = View.VISIBLE
            this.dividerSecond.visibility = View.VISIBLE
            return this
        }

        fun setSecondText(secondText: String): Builder {
            this.mTvSecond.text = secondText
            this.mTvSecond.visibility = View.VISIBLE
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

    companion object {

        const val GRAVITY_BOTTOM = 0
        const val GRAVITY_CENTER = 1
        const val GRAVITY_TOP = 2
        const val TOAST_SHORT = 0
        const val TOAST_LONG = 1
    }


}
