package com.wrbug.developerhelper.commonutil

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.view.View

class SpannableBuilder private constructor(private val context: Context, private val text: String) {

    private val spanMap = hashMapOf<String, ArrayList<Pair<Int, Any>>>()

    companion object {
        fun with(context: Context, strRes: Int): SpannableBuilder {
            return SpannableBuilder(context, strRes)
        }

        fun with(context: Context, text: String): SpannableBuilder {
            return SpannableBuilder(context, text)
        }
    }

    private var strRes: Int = -1

    private constructor(context: Context, strRes: Int) : this(context, "") {
        this.strRes = strRes
    }

    fun addSpanWithTextAppearance(
        value: String,
        textAppearance: Int,
        color: Int? = null,
        index: Int = 0
    ): SpannableBuilder {
        addSpan(value, TextAppearanceSpan(context, textAppearance), index)
        if (color != null) {
            addSpan(value, ForegroundColorSpan(color), index)
        }
        return this
    }

    fun addSpanWithClickListener(
        key: String,
        linkColor: Int,
        index: Int = 0,
        listener: () -> Unit
    ): SpannableBuilder {
        addSpan(key, object : ClickableSpan() {
            override fun onClick(widget: View) {
                listener()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = true
                ds.color = linkColor
            }
        }, index)
        return this
    }

    fun addSpanWithColor(value: String, color: Int, index: Int = 0): SpannableBuilder {
        addSpan(value, ForegroundColorSpan(color), index)
        return this
    }


    fun addSpanWithDeleteLine(value: String, index: Int = 0): SpannableBuilder {
        addSpan(value, StrikethroughSpan(), index)
        return this
    }

    fun addSpanWithBold(value: String, index: Int = 0): SpannableBuilder {
        addSpan(value, StyleSpan(Typeface.BOLD), index)
        return this
    }

    fun addCustomSpan(value: String, what: Any, index: Int = 0): SpannableBuilder {
        addSpan(value, what, index)
        return this
    }

    private fun addSpan(key: String, what: Any, index: Int) {
        spanMap[key] = (spanMap[key] ?: arrayListOf()).apply { add(index to what) }
    }

    fun build(): Spannable {
        val originStr = if (strRes == -1) {
            text
        } else {
            context.getString(strRes)
        }
        val spannableBuilder = SpannableStringBuilder(originStr)
        spanMap.forEach { (key, pair) ->
            pair.forEach { (index, what) ->
                val strIndex = index.let {
                    var i = it
                    var currentIndex = 0
                    while (i != 0) {
                        i--
                        currentIndex = originStr.indexOf(key, currentIndex) + key.length
                    }
                    originStr.indexOf(key, currentIndex)
                }
                if (strIndex == -1) {
                    return@forEach
                }
                spannableBuilder.setSpan(
                    what,
                    strIndex,
                    strIndex + key.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

        }
        return spannableBuilder
    }
}