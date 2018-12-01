package com.wrbug.developerhelper.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.wrbug.developerhelper.HierarchyNode
import com.wrbug.developerhelper.util.JsonHelper
import java.util.HashMap

class HierarchyView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val strokePaint = Paint()
    private val contentPaint = Paint()
    private var mHierarchyNodes = HashMap<Long, HierarchyNode>()
    private var onHierarchyNodeClickListener: OnHierarchyNodeClickListener? = null

    constructor(context: Context) : this(context, null)

    init {
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = Color.RED
        contentPaint.color = Color.argb(10, 0, 0, 0)
    }

    fun setHierarchyNodes(hierarchyNodes: HashMap<Long, HierarchyNode>?) {
        mHierarchyNodes.clear()
        if (hierarchyNodes != null) {
            mHierarchyNodes.putAll(hierarchyNodes)
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                val hierarchyNode = getNode(event.x, event.y)
                if (hierarchyNode != null) {
                    Log.i("aaaa", JsonHelper.toJson(hierarchyNode))
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        for (hierarchyNode in mHierarchyNodes) {
            drawWidget(canvas, hierarchyNode.value)
        }
    }

    private fun drawWidget(canvas: Canvas?, hierarchyNode: HierarchyNode) {
        val mBounds = hierarchyNode.bounds
        canvas?.drawRect(mBounds, contentPaint)
        canvas?.drawRect(mBounds, strokePaint)
    }

    fun getNode(x: Float, y: Float): HierarchyNode? {
        for (hierarchyNode in mHierarchyNodes.values) {
            val node = getNode(x, y, hierarchyNode)
            if (node != null) {
                return node
            }
        }
        return null
    }

    fun getNode(x: Float, y: Float, hierarchyNode: HierarchyNode): HierarchyNode? {
        val rect = hierarchyNode.bounds ?: return null
        if (rect.left < x && rect.right >= x && rect.top < y && rect.bottom >= y) {
            if (!hierarchyNode.childId.isEmpty()) {
                for (id in hierarchyNode.childId) {
                    val node = getNode(x, y, mHierarchyNodes[id]!!)
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
        fun onClick(node: HierarchyNode)
    }
}