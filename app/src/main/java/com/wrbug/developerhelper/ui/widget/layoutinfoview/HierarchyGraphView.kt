package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewParent
import androidx.viewpager.widget.ViewPager
import de.blox.graphview.GraphView
import java.util.jar.Attributes

class HierarchyGraphView(context: Context, attributes: AttributeSet?) : GraphView(context, attributes) {
    private var clickTime = 0L
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        var parent: ViewParent = this
        while (parent !is ViewPager) {
            parent = parent.parent
        }
        parent.requestDisallowInterceptTouchEvent(true)
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - clickTime < 200) {
                zoomBy(1.8F, true)
                clickTime = 0
            }
            clickTime = System.currentTimeMillis()

        }
        return super.dispatchTouchEvent(ev)
    }
}