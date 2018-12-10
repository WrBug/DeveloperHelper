package com.wrbug.developerhelper.ui.widget.settingitemview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.wrbug.developerhelper.R
import kotlinx.android.synthetic.main.view_setting_item.view.*

class SettingItemView : FrameLayout {
    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
        initAttrs(attrs)
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_setting_item, this)
        setBackgroundResource(R.drawable.ripple_with_color_mask)
        switcher.setOnTouchListener { _, _ -> true }
    }

    private fun initAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingItemView)
        val src = typedArray.getDrawable(R.styleable.SettingItemView_src)
        src?.let {
            setImage(it)
        }
        val title = typedArray.getString(R.styleable.SettingItemView_title)
        titleTv.text = title
        val summary = typedArray.getString(R.styleable.SettingItemView_summary)
        summary.takeIf {
            !TextUtils.isEmpty(it)
        }?.let {
            summaryTv.text = it
            summaryTv.visibility = View.VISIBLE
        }
        val switchVisible = typedArray.getBoolean(R.styleable.SettingItemView_switchVisible, true)
        switcher.visibility = if (switchVisible) View.VISIBLE else View.GONE
        switcher.isChecked = typedArray.getBoolean(R.styleable.SettingItemView_checked, false)
        typedArray.recycle()
    }


    fun setImage(drawable: Drawable) {
        icoIv.setImageDrawable(drawable)
        icoIv.visibility = View.VISIBLE
    }
}