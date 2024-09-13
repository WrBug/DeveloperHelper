package com.wrbug.developerhelper.ui.widget.settingitemview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.dpInt
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener
import com.wrbug.developerhelper.databinding.ViewSettingItemBinding

class SettingItemView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

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
    private val binding = ViewSettingItemBinding.inflate(LayoutInflater.from(context), this)

    init {
        initView()
        initAttrs(attrs)
    }

    private fun initView() {
        updatePadding(top = 8.dpInt(context), bottom = 8.dpInt(context))
        setBackgroundResource(R.drawable.ripple_with_color_mask)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs ?: return
        with(context.obtainStyledAttributes(attrs, R.styleable.SettingItemView)) {
            val src = getDrawable(R.styleable.SettingItemView_src)
            setImage(src)
            getColorStateList(R.styleable.SettingItemView_icoTint)?.let {
                setIconTint(it)
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

    private fun setIconTint(colorStateList: ColorStateList?) {
        binding.icoIv.imageTintList = colorStateList
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        if (switcherMaskViewClicked.not()) {
            binding.switcherMaskView.setOnClickListener(l)
        }
    }

    fun setOnSwitcherClickListener(listener: View.() -> Unit) {
        switcherMaskViewClicked = true
        binding.switcherMaskView.setOnDoubleCheckClickListener(clickListener = listener)
    }

    fun isChecked() = binding.switcher.isChecked

    private fun setSwitchCheckable() {
        binding.switcherMaskView.isGone = checkable
    }

    fun setOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
        binding.switcher.setOnCheckedChangeListener(listener)
    }

    fun setImage(drawable: Drawable?) {
        binding.icoIv.setImageDrawable(drawable)
        binding.icoIv.isVisible = drawable != null
    }

    fun setSummary(summary: String?) {
        binding.summaryTv.text = summary
        binding.summaryTv.isVisible = !summary.isNullOrEmpty()
    }

}