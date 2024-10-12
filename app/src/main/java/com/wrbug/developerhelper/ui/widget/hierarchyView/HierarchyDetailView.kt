package com.wrbug.developerhelper.ui.widget.hierarchyView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import com.wrbug.developerhelper.base.entry.HierarchyNode
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.ui.widget.helper.CanvasHelper
import com.wrbug.developerhelper.commonutil.UiUtils

class HierarchyDetailView : FrameLayout {

    private val paint: Paint by lazy {
        val paint = Paint()
        paint.color = context.resources.getColor(R.color.material_color_blue_800)
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
    }

    private fun initView() {
        setBackgroundColor(Color.TRANSPARENT)
        setWillNotDraw(false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (hierarchyNode != null) {
            if (hierarchyNode?.parentId!! > -1) {
                drawParentNode(canvas)
            }
            drawNode(canvas)
        }
    }

    private fun drawParentNode(canvas: Canvas?) {
        if (parentHierarchyNode == null) {
            return
        }
        val bounds = parentHierarchyNode?.screenBounds ?: return
        canvas?.drawRect(bounds, parentpPaint)

    }

    private fun drawNode(canvas: Canvas?) {
        hierarchyNode?.screenBounds?.let {
            canvas?.drawRect(it, paint)
            CanvasHelper.drawAL(
                it.left ?: 0,
                0,
                (it.top + it.bottom) / 2,
                (it.top + it.bottom) / 2,
                canvas,
                paint
            )
            CanvasHelper.drawAL(
                (it.left + it.right) / 2,
                (it.left + it.right) / 2,
                it.top,
                0,
                canvas,
                paint
            )
            CanvasHelper.drawAL(
                it.right,
                UiUtils.getDeviceWidth(),
                (it.top + it.bottom) / 2,
                (it.top + it.bottom) / 2,
                canvas,
                paint
            )
            CanvasHelper.drawAL(
                (it.left + it.right) / 2,
                (it.left + it.right) / 2,
                it.bottom,
                UiUtils.getDeviceHeight(),
                canvas,
                paint
            )
        }

    }

}