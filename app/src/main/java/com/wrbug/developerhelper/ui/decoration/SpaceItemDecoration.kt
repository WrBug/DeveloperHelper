package com.wrbug.developerhelper.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wrbug.developerhelper.commonutil.dpInt

/**
 * SpaceItemDecoration
 *
 * @author wrbug
 * @since 2017/9/29
 */
class SpaceItemDecoration(
    private val leftSpace: Int = 0,
    private val topSpace: Int = 0,
    private val rightSpace: Int = 0,
    private val bottomSpace: Int = 0,
    private var firstTopSpace: Int = 0,
    private var lastBottomSpace: Int = 0,
) : RecyclerView.ItemDecoration() {
    companion object {
        val standard = SpaceItemDecoration(
            24.dpInt(),
            12.dpInt(),
            24.dpInt(),
            12.dpInt(),
            24.dpInt(),
            40.dpInt()
        )
    }

    constructor(space: Int) : this(
        bottomSpace = space,
        topSpace = space,
        rightSpace = space,
        leftSpace = space,
        lastBottomSpace = space,
        firstTopSpace = space
    )

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
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


    fun setLastBottomPadding(space: Int) {
        lastBottomSpace = space
    }

    fun setFirstTopPadding(space: Int) {
        firstTopSpace = space
    }
}
