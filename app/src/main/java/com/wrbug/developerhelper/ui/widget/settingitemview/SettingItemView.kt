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
import com.wrbug.developerhelper.databinding.ViewSettingItemBinding

class SettingItemView: FrameLayout {

    var checkable: Boolean = true
        set(value) {
            field = value
            setSwitchCheckable()
        }

    var checked: Boolean = false
        set(value) {
            field = value
            binding.switcher.isChecked = checked
        }
    private var switcherMaskViewClicked = false
    private val binding: ViewSettingItemBinding by lazy {
        ViewSettingItemBinding.inflate(LayoutInflater.from(context), this, true)
    }

    constructor(context: Context): super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        initView()
        initAttrs(attrs)
    }

    private fun initView() {
        setBackgroundResource(R.drawable.ripple_with_color_mask)
    }

    private fun initAttrs(attrs: AttributeSet) {
        with(context.obtainStyledAttributes(attrs, R.styleable.SettingItemView)) {
            val src = getDrawable(R.styleable.SettingItemView_src)
            src?.let {
                setImage(it)
            }
            val title = getString(R.styleable.SettingItemView_title)
            binding.titleTv.text = title
            setSummary(getString(R.styleable.SettingItemView_summary))
            val switchVisible = getBoolean(R.styleable.SettingItemView_switchVisible, true)
            binding.switcher.visibility = if (switchVisible) View.VISIBLE else View.GONE
            checked = getBoolean(R.styleable.SettingItemView_checked, false)
            checkable = getBoolean(R.styleable.SettingItemView_checkable, true)
            recycle()
        }

    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        if (switcherMaskViewClicked.not()) {
            binding.switcherMaskView.setOnClickListener(l)
        }
    }

    fun setOnSwitcherClickListener(listener: View.() -> Unit) {
        switcherMaskViewClicked = true
        binding.switcherMaskView.setOnClickListener(listener)
    }

    fun isChecked() = binding.switcher.isChecked

    private fun setSwitchCheckable() {
        if (checkable) {
//            switcher.setOnTouchListener(null)
            binding.switcherMaskView.visibility = View.GONE
        } else {
//            switcher.setOnTouchListener { _, _ -> true }
            binding.switcherMaskView.visibility = View.VISIBLE
        }
    }

    fun setOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
        binding.switcher.setOnCheckedChangeListener(listener)
    }

    fun setImage(drawable: Drawable) {
        binding.icoIv.setImageDrawable(drawable)
        binding.icoIv.visibility = View.VISIBLE
    }

    fun setSummary(summary: String?) {
        summary.takeIf {
            !TextUtils.isEmpty(it)
        }?.let {
            binding.summaryTv.text = it
            binding.summaryTv.visibility = View.VISIBLE
        }
    }

}