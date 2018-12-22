package com.wrbug.developerhelper.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * SpaceItemDecoration
 *
 * @author wrbug
 * @since 2017/9/29
 */
class SpaceItemDecoration : RecyclerView.ItemDecoration {
    private var leftSpace: Int = 0
    private var topSpace: Int = 0
    private var rightSpace: Int = 0
    private var bottomSpace: Int = 0
    private var firstTopSpace = 0
    private var lastBottomSpace: Int = 0

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.left = leftSpace
        outRect.right = rightSpace
        outRect.bottom = bottomSpace
        outRect.top = topSpace
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = firstTopSpace
        }
        parent.adapter?.let {
            if (lastBottomSpace > 0 && parent.getChildAdapterPosition(view) == it.itemCount - 1) {
                outRect.bottom = lastBottomSpace
            }
        }

    }

    constructor(space: Int) {
        bottomSpace = space
        topSpace = space
        rightSpace = space
        leftSpace = space
        lastBottomSpace = space
        firstTopSpace = space
    }

    fun setLastBottomPadding(space: Int) {
        lastBottomSpace = space
    }

    fun setFirstTopPadding(space: Int) {
        firstTopSpace = space
    }

    constructor(leftSpace: Int, topSpace: Int, rightSpace: Int, bottomSpace: Int) {
        this.leftSpace = leftSpace
        this.topSpace = topSpace
        this.rightSpace = rightSpace
        this.bottomSpace = bottomSpace
        lastBottomSpace = bottomSpace
        firstTopSpace = topSpace
    }
}
