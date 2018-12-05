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
import com.wrbug.developerhelper.ui.widget.layoutinfoview.LayoutInfoView

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
    private val layoutInfoView: LayoutInfoView by lazy {
        val layoutInfoView = LayoutInfoView(context)
        val params = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.BOTTOM
        addView(layoutInfoView, params)
        layoutInfoView
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
        layoutInfoView.visibility = View.VISIBLE
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
    }
}