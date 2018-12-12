package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.model.entry.HierarchyNode
import kotlinx.android.synthetic.main.view_layout_info.*


class LayoutInfoView(context: Context, private val hierarchyNode: HierarchyNode) : BottomSheetDialog(context) {

    init {
        init()
    }


    private fun init() {
        setContentView(R.layout.view_layout_info)
        initViewpager()
    }

    private fun initViewpager() {
        val adapter = ViewPagerAdapter(context,hierarchyNode)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }
}
