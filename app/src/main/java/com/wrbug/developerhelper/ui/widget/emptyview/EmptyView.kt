package com.wrbug.developerhelper.ui.widget.emptyview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.UiUtils

class EmptyView : LinearLayout {
    val icoIv: ImageView by lazy {
        val icoIv = ImageView(context)
        icoIv.setImageResource(R.drawable.ic_ic_empty)
        icoIv
    }
    val textTv: TextView by lazy {
        val tv = TextView(context)
        tv.setTextColor(resources.getColor(R.color.text_color_666666))
        tv.text = "无数据"
        tv.textSize = 20F
        tv.gravity = Gravity.CENTER
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.topMargin = UiUtils.dp2px(context, 20F)
        tv.layoutParams = params
        tv
    }

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init()
        initView()
    }

    private fun init() {
        orientation = VERTICAL
        gravity = Gravity.CENTER
    }

    private fun initView() {
        addView(icoIv)
        addView(textTv)
    }


}