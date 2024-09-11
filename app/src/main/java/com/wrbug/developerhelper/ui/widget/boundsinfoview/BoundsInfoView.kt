package com.wrbug.developerhelper.ui.widget.boundsinfoview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.ui.widget.helper.CanvasHelper
import com.wrbug.developerhelper.commonutil.UiUtils

class BoundsInfoView : View {
    var bounds: Rect? = null
        set(value) {
            field = value
            invalidate()
        }
    private val edgeLineSize = UiUtils.dp2px(context, 4F).toFloat()
    private val paint = Paint()
    private var unit: Unit = Unit.DP
        set(value) {
            if (field == value) {
                return
            }
            field = value
            invalidate()
        }
    private val textMargin = UiUtils.dp2px(context, 3F)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setOnClickListener {
            toggleUnit()
        }
    }

    private fun toggleUnit() {
        unit = if (unit == Unit.DP) Unit.PX else Unit.DP
    }

    override fun onDraw(canvas: Canvas) {
        val rect = drawRect(canvas)
        val lineRect = drawEdge(rect, canvas)
        drawAl(rect, lineRect, canvas)
        drawRectSize(rect, canvas)
        drawMarginSize(lineRect, canvas)
    }

    private fun drawMarginSize(lineRect: RectF, canvas: Canvas?) {
        bounds?.let {
            paint.reset()
            paint.isAntiAlias = true
            paint.textSize = UiUtils.dp2px(context, 14F).toFloat()
            var leftMargin = it.left
            var topMargin = it.top
            var rightMargin = UiUtils.getDeviceWidth() - it.right
            var bottomMargin = UiUtils.getDeviceHeight() - it.bottom
            if (unit == Unit.DP) {
                leftMargin = UiUtils.px2dp(context, leftMargin.toFloat()).toInt()
                topMargin = UiUtils.px2dp(context, topMargin.toFloat()).toInt()
                rightMargin = UiUtils.px2dp(context, rightMargin.toFloat()).toInt()
                bottomMargin = UiUtils.px2dp(context, bottomMargin.toFloat()).toInt()
            }
            val leftMarginText = "$leftMargin ${unit.s}"
            val topMarginText = "$topMargin ${unit.s}"
            val rightMarginText = "$rightMargin ${unit.s}"
            val bottomMarginText = "$bottomMargin ${unit.s}"
            val bounds = Rect()
            paint.getTextBounds(leftMarginText, 0, leftMarginText.length, bounds)
            canvas?.drawText(
                leftMarginText,
                lineRect.left - bounds.width() - edgeLineSize / 2 - textMargin,
                (lineRect.top + lineRect.bottom) / 2 + bounds.height() / 2,
                paint
            )

            paint.getTextBounds(topMarginText, 0, topMarginText.length, bounds)
            canvas?.drawText(
                topMarginText,
                (lineRect.left + lineRect.right) / 2 - bounds.width() / 2,
                lineRect.top - edgeLineSize / 2 - textMargin,
                paint
            )

            paint.getTextBounds(rightMarginText, 0, rightMarginText.length, bounds)
            canvas?.drawText(
                rightMarginText,
                lineRect.right + edgeLineSize / 2 + textMargin,
                (lineRect.top + lineRect.bottom) / 2 + bounds.height() / 2,
                paint
            )

            paint.getTextBounds(bottomMarginText, 0, bottomMarginText.length, bounds)
            canvas?.drawText(
                bottomMarginText,
                (lineRect.left + lineRect.right) / 2 - bounds.width() / 2,
                lineRect.bottom + bounds.height() + edgeLineSize / 2 + textMargin,
                paint
            )
        }
    }

    private fun drawRectSize(rect: RectF, canvas: Canvas?) {
        bounds?.let {
            var width = it.right - it.left
            var height = it.bottom - it.top
            if (unit == Unit.DP) {
                width = UiUtils.px2dp(context, width.toFloat()).toInt()
                height = UiUtils.px2dp(context, height.toFloat()).toInt()
            }
            val text = "$width ${unit.s} × $height ${unit.s}"
            paint.reset()
            paint.isAntiAlias = true
            paint.textSize = UiUtils.dp2px(context, 14F).toFloat()
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas?.drawText(
                text,
                (rect.left + rect.right) / 2 - textWidth / 2,
                (rect.top + rect.bottom) / 2 + textHeight / 2,
                paint
            )
        }

    }

    private fun drawAl(rect: RectF, lineRect: RectF, canvas: Canvas?) {
        canvas?.run {
            paint.reset()
            paint.style = Paint.Style.STROKE
            paint.isAntiAlias = true
            paint.color = resources.getColor(R.color.colorAccent)
            paint.strokeWidth = UiUtils.dp2px(context, 1F).toFloat()
            CanvasHelper.drawAL(
                rect.left,
                lineRect.left + edgeLineSize / 2,
                (rect.top + rect.bottom) / 2,
                (rect.top + rect.bottom) / 2,
                canvas,
                paint
            )

            CanvasHelper.drawAL(
                (rect.left + rect.right) / 2,
                (rect.left + rect.right) / 2,
                rect.top,
                lineRect.top + edgeLineSize / 2,
                canvas,
                paint
            )

            CanvasHelper.drawAL(
                rect.right,
                lineRect.right - edgeLineSize / 2,
                (rect.top + rect.bottom) / 2,
                (rect.top + rect.bottom) / 2,
                canvas,
                paint
            )

            CanvasHelper.drawAL(
                (rect.left + rect.right) / 2,
                (rect.left + rect.right) / 2,
                rect.bottom,
                lineRect.bottom - edgeLineSize / 2,
                canvas,
                paint
            )
        }
    }

    private fun drawEdge(rect: RectF, canvas: Canvas?): RectF {
        paint.reset()
        paint.color = resources.getColor(R.color.colorAccentLight)
        paint.strokeWidth = edgeLineSize
        paint.isAntiAlias = true
        val lineWidth = measuredWidth / 6
        val lineHeight = measuredHeight / 6
        val lineRect =
            RectF(
                rect.left / 2F,
                rect.top / 2F,
                measuredWidth - rect.left / 2F,
                measuredHeight - rect.top / 2F
            )
        canvas?.run {
            drawLine(
                lineRect.left,
                (rect.top + rect.bottom) / 2F - lineHeight / 2F,
                lineRect.left,
                (rect.top + rect.bottom) / 2F + lineHeight / 2F,
                paint
            )
            drawLine(
                (rect.left + rect.right) / 2F - lineWidth / 2F,
                lineRect.top,
                (rect.left + rect.right) / 2F + lineWidth / 2F,
                lineRect.top,
                paint
            )
            drawLine(
                lineRect.right,
                (rect.top + rect.bottom) / 2F - lineHeight / 2F,
                lineRect.right,
                (rect.top + rect.bottom) / 2F + lineHeight / 2F,
                paint
            )
            drawLine(
                (rect.left + rect.right) / 2F - lineWidth / 2F,
                lineRect.bottom,
                (rect.left + rect.right) / 2F + lineWidth / 2F,
                lineRect.bottom,
                paint
            )
        }
        return lineRect
    }

    private fun drawRect(canvas: Canvas?): RectF {
        paint.reset()
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.color = resources.getColor(R.color.colorAccent)
        paint.strokeWidth = UiUtils.dp2px(context, 2F).toFloat()
        val width = measuredWidth / 3F
        val height = measuredHeight / 4F
        val top = measuredHeight * 3 / 8F
        val rect = RectF(width, top, width * 2, height + top)
        canvas?.drawRect(rect, paint)
        return rect
    }


    enum class Unit(var s: String) {
        DP("dp"), PX("px")
    }
}
