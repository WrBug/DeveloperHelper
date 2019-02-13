package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewParent
import androidx.viewpager.widget.ViewPager
import de.blox.graphview.GraphView
import java.util.jar.Attributes

class HierarchyGraphView(context: Context, attributes: AttributeSet?) : GraphView(context, attributes) {
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        var parent: ViewParent = this
        while (parent !is ViewPager) {
            parent = parent.parent
        }
        parent.requestDisallowInterceptTouchEvent(true)
        return super.dispatchTouchEvent(ev)
    }
}