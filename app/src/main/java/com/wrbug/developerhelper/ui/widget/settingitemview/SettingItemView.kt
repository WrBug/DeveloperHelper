package com.wrbug.developerhelper.ui.widget.settingitemview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import com.wrbug.developerhelper.R
import kotlinx.android.synthetic.main.view_setting_item.view.*

class SettingItemView : FrameLayout {
    var checkable: Boolean = true
        set(value) {
            field = value
            setSwitchCheckable()
        }

    var checked: Boolean = false
        set(value) {
            field = value
            switcher.isChecked = checked
        }


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
    }

    private fun initAttrs(attrs: AttributeSet) {
        with(context.obtainStyledAttributes(attrs, R.styleable.SettingItemView)) {
            val src = getDrawable(R.styleable.SettingItemView_src)
            src?.let {
                setImage(it)
            }
            val title = getString(R.styleable.SettingItemView_title)
            titleTv.text = title
            setSummary(getString(R.styleable.SettingItemView_summary))
            val switchVisible = getBoolean(R.styleable.SettingItemView_switchVisible, true)
            switcher.visibility = if (switchVisible) View.VISIBLE else View.GONE
            checked = getBoolean(R.styleable.SettingItemView_checked, false)
            checkable = getBoolean(R.styleable.SettingItemView_checkable, true)
            recycle()
        }

    }

    private fun setSwitchCheckable() {
        if (checkable) {
            switcher.setOnTouchListener(null)
        } else {
            switcher.setOnTouchListener { _, _ -> true }
        }
    }

    fun setOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
        switcher.setOnCheckedChangeListener(listener)
    }

    fun setImage(drawable: Drawable) {
        icoIv.setImageDrawable(drawable)
        icoIv.visibility = View.VISIBLE
    }

    fun setSummary(summary: String?) {
        summary.takeIf {
            !TextUtils.isEmpty(it)
        }?.let {
            summaryTv.text = it
            summaryTv.visibility = View.VISIBLE
        }
    }


}