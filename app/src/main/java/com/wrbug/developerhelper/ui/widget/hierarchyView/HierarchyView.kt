package com.wrbug.developerhelper.ui.widget.hierarchyView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.wrbug.developerhelper.model.entity.HierarchyNode

class HierarchyView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val strokePaint = Paint()
    private val contentPaint = Paint()
    private var mHierarchyNodes = arrayListOf<HierarchyNode>()
    private var onHierarchyNodeClickListener: OnHierarchyNodeClickListener? = null

    constructor(context: Context) : this(context, null)

    init {
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 2F
        strokePaint.color = Color.GREEN
        contentPaint.color = Color.argb(15, 0, 0, 0)
    }

    fun setHierarchyNodes(hierarchyNodes: List<HierarchyNode>?) {
        mHierarchyNodes.clear()
        hierarchyNodes?.let {
            mHierarchyNodes.addAll(it)
        }
        invalidate()
    }

    fun setOnHierarchyNodeClickListener(listener: OnHierarchyNodeClickListener) {
        onHierarchyNodeClickListener = listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val hierarchyNode = getNode(event.x, event.y)
                if (hierarchyNode != null && onHierarchyNodeClickListener != null) {
                    onHierarchyNodeClickListener?.onClick(hierarchyNode.selectedNode, hierarchyNode.parentNode)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        drawRect(canvas, mHierarchyNodes)
    }

    private fun drawRect(canvas: Canvas?, hierarchyNodes: List<HierarchyNode>) {
        for (hierarchyNode in hierarchyNodes) {
            drawWidget(canvas, hierarchyNode)
            drawRect(canvas, hierarchyNode.childId)
        }
    }

    private fun drawWidget(canvas: Canvas?, hierarchyNode: HierarchyNode) {
        hierarchyNode.screenBounds?.let {
            canvas?.drawRect(it, contentPaint)
            canvas?.drawRect(it, strokePaint)
        }

    }

    fun getNode(x: Float, y: Float): SelectedNodeInfo? {
        for (hierarchyNode in mHierarchyNodes) {
            val node = getNode(x, y, hierarchyNode)
            if (node != null) {
                return node
            }
        }
        return null
    }

    private fun getNode(x: Float, y: Float, hierarchyNode: HierarchyNode): SelectedNodeInfo? {
        val rect = hierarchyNode.screenBounds ?: return null
        if (rect.contains(x.toInt(), y.toInt())) {
            if (!hierarchyNode.childId.isEmpty()) {
                for (child in hierarchyNode.childId.reversed()) {
                    val node = getNode(x, y, child)
                    if (node != null) {
                        return SelectedNodeInfo(node.selectedNode, hierarchyNode)
                    }
                }
            }
            return SelectedNodeInfo(hierarchyNode, null)
        }
        return null
    }

    interface OnHierarchyNodeClickListener {
        fun onClick(node: HierarchyNode, parentNode: HierarchyNode?)
    }
}