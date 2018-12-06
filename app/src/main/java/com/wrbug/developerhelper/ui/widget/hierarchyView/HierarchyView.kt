package com.wrbug.developerhelper.ui.widget.hierarchyView

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.wrbug.developerhelper.model.entry.HierarchyNode

class HierarchyView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val strokePaint = Paint()
    private val contentPaint = Paint()
    private var mHierarchyNodeMap: LinkedHashMap<Long, HierarchyNode> = LinkedHashMap()
    private var onHierarchyNodeClickListener: OnHierarchyNodeClickListener? = null

    constructor(context: Context) : this(context, null)

    init {
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = 2F
        strokePaint.color = Color.GREEN
        contentPaint.color = Color.argb(20, 0, 0, 0)
    }

    fun setHierarchyNodes(hierarchyNodes: Map<Long, HierarchyNode>?) {
        mHierarchyNodeMap.clear()
        if (hierarchyNodes != null) {
            mHierarchyNodeMap.putAll(hierarchyNodes.toSortedMap(Comparator { _, _ -> -1 }))
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
                    onHierarchyNodeClickListener?.onClick(hierarchyNode, mHierarchyNodeMap[hierarchyNode.parentId])
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        for (hierarchyNode in mHierarchyNodeMap) {
            drawWidget(canvas, hierarchyNode.value)
        }
    }

    private fun drawWidget(canvas: Canvas?, hierarchyNode: HierarchyNode) {
        val mBounds = hierarchyNode.screenBounds
        canvas?.drawRect(mBounds, contentPaint)
        canvas?.drawRect(mBounds, strokePaint)
    }

    fun getNode(x: Float, y: Float): HierarchyNode? {
        for (hierarchyNode in mHierarchyNodeMap.values) {
            val node = getNode(x, y, hierarchyNode)
            if (node != null) {
                return node
            }
        }
        return null
    }

    private fun getNode(x: Float, y: Float, hierarchyNode: HierarchyNode): HierarchyNode? {
        val rect = hierarchyNode.screenBounds ?: return null
        if (rect.left < x && rect.right >= x && rect.top < y && rect.bottom >= y) {
            if (!hierarchyNode.childId.isEmpty()) {
                for (id in hierarchyNode.childId) {
                    val node = getNode(x, y, mHierarchyNodeMap[id]!!)
                    if (node != null) {
                        return node
                    }
                }
            }
            return hierarchyNode
        }
        return null
    }

    interface OnHierarchyNodeClickListener {
        fun onClick(node: HierarchyNode, parentNode: HierarchyNode?)
    }
}