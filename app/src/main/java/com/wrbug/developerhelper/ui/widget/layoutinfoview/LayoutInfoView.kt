package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.model.entity.HierarchyNode
import com.wrbug.developerhelper.commonutil.UiUtils
import kotlinx.android.synthetic.main.view_layout_info.*


class LayoutInfoView(
    context: Context,
    private val nodeList: List<HierarchyNode>?,
    private val hierarchyNode: HierarchyNode
) : BottomSheetDialog(context) {
    val adapter = LayoutInfoViewPagerAdapter(context, nodeList, hierarchyNode)

    init {
        init()
    }

    fun setOnNodeChangedListener(listener: OnNodeChangedListener) {
        adapter.setOnNodeChangedListener(listener)
    }

    private fun init() {
        setContentView(R.layout.view_layout_info)
        val layoutParams = layoutInfoContainer.layoutParams
        layoutParams.height = UiUtils.getDeviceHeight(context) / 2
        layoutInfoContainer.layoutParams = layoutParams
        initViewpager()
    }

    private fun initViewpager() {
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}
