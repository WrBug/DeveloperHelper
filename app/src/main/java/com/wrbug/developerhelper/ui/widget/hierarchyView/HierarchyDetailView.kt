package com.wrbug.developerhelper.ui.widget.hierarchyView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.wrbug.developerhelper.model.entry.HierarchyNode
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.ui.widget.helper.CanvasHelper
import com.wrbug.developerhelper.ui.widget.layoutinfoview.LayoutInfoView
import com.wrbug.developerhelper.util.UiUtils

class HierarchyDetailView : FrameLayout {
    private val paint: Paint by lazy {
        val paint = Paint()
        paint.color = context.resources.getColor(R.color.colorAccent)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3F
        paint
    }
    private val parentpPaint: Paint by lazy {
        val paint = Paint()
        paint.color = context.resources.getColor(R.color.colorAccentLight)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3F
        paint
    }

    private var parentHierarchyNode: HierarchyNode? = null
    private var hierarchyNode: HierarchyNode? = null

    constructor(context: Context) : super(context) {
        initView()
    }


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    fun setNode(hierarchyNode: HierarchyNode, parentHierarchyNode: HierarchyNode?) {
        this.hierarchyNode = hierarchyNode
        this.parentHierarchyNode = parentHierarchyNode
        invalidate()
        LayoutInfoView(context,hierarchyNode).show()
    }

    private fun initView() {
        setBackgroundColor(Color.TRANSPARENT)
        setWillNotDraw(false)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (hierarchyNode != null) {
            drawNode(canvas)
            if (hierarchyNode?.parentId!! > -1) {
                drawParentNode(canvas)
            }
        }
    }

    private fun drawParentNode(canvas: Canvas?) {
        if (parentHierarchyNode == null) {
            return
        }
        val bounds = parentHierarchyNode?.screenBounds
        canvas?.drawRect(bounds, parentpPaint)

    }

    private fun drawNode(canvas: Canvas?) {
        val bounds = hierarchyNode?.screenBounds
        canvas?.drawRect(bounds, paint)
        CanvasHelper.drawAL(
            bounds?.left ?: 0,
            0,
            (bounds?.top!! + bounds.bottom) / 2,
            (bounds?.top!! + bounds.bottom) / 2,
            canvas,
            paint
        )
        CanvasHelper.drawAL(
            (bounds.left + bounds.right) / 2,
            (bounds.left + bounds.right) / 2,
            bounds.top,
            0,
            canvas,
            paint
        )
        CanvasHelper.drawAL(
            bounds.right,
            UiUtils.getDeviceWidth(),
            (bounds?.top + bounds.bottom) / 2,
            (bounds?.top!! + bounds.bottom) / 2,
            canvas,
            paint
        )
        CanvasHelper.drawAL(
            (bounds.left + bounds.right) / 2,
            (bounds.left + bounds.right) / 2,
            bounds.bottom,
            UiUtils.getDeviceHeight(),
            canvas,
            paint
        )
    }

    private fun drawLine(startX: Int, endX: Int, startY: Int, endY: Int, canvas: Canvas?, paint: Paint) {
        canvas?.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), paint)
    }
}