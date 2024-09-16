package com.wrbug.developerhelper.ui.widget.appbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.databinding.ViewAppBarBinding
import com.wrbug.developerhelper.util.getColor
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener

class AppBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewAppBarBinding.inflate(LayoutInflater.from(context), this, false)

    init {
        setBackgroundColor(R.color.colorPrimaryDark.getColor(context))
        addView(binding.root)
        binding.ivBack.setOnDoubleCheckClickListener {
            if (context is FragmentActivity) {
                context.onBackPressedDispatcher.onBackPressed()
            }
        }
        attrs?.let {
            initAttr(it)
        }
    }

    private fun initAttr(attrs: AttributeSet) {
        with(context.obtainStyledAttributes(attrs, R.styleable.AppBar)) {
            val title = getString(R.styleable.AppBar_title)
            setTitle(title)
            val subTitle = getString(R.styleable.AppBar_subTitle)
            setSubTitle(subTitle)
            val showBack = getBoolean(R.styleable.AppBar_showBack, false)
            showBack(showBack)
            recycle()
        }
    }

    fun setSubTitle(subTitle: CharSequence?) {
        binding.tvSubTitle.text = subTitle
        binding.tvSubTitle.isGone = subTitle.isNullOrEmpty()
    }

    fun showBack(showBack: Boolean) {
        binding.ivBack.isInvisible = !showBack
    }

    fun setTitle(title: CharSequence?) {
        binding.tvTitle.text = title
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child == binding.root) {
            super.addView(child, index, params)
            return
        }
        binding.flRight.addView(child, index, params)
    }

    override fun addViewInLayout(
        child: View?,
        index: Int,
        params: ViewGroup.LayoutParams?,
        preventRequestLayout: Boolean
    ): Boolean {
        return super.addViewInLayout(child, index, params, preventRequestLayout)
    }


//    fun setMenu(title: String?, listener: (View) -> Unit) {
//        binding.tvRightBtn.text = title
//        binding.tvRightBtn.isGone = title.isNullOrEmpty()
//        binding.tvRightBtn.setOnDoubleCheckClickListener {
//            listener(it)
//        }
//    }
}