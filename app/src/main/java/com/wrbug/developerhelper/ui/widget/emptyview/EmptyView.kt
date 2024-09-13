package com.wrbug.developerhelper.ui.widget.emptyview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.databinding.ViewEmptyViewBinding

class EmptyView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    private val binding = ViewEmptyViewBinding.inflate(LayoutInflater.from(context), this)

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }
}