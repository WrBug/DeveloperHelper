package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wrbug.developerhelper.basecommon.entry.HierarchyNode
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.databinding.ViewLayoutInfoBinding

class LayoutInfoView(
    context: Context,
    private val nodeList: List<HierarchyNode>?,
    private val hierarchyNode: HierarchyNode
): BottomSheetDialog(context) {

    val adapter = LayoutInfoViewPagerAdapter(context, nodeList, hierarchyNode)

    init {
        init()
    }

    private lateinit var binding: ViewLayoutInfoBinding
    fun setOnNodeChangedListener(listener: OnNodeChangedListener) {
        adapter.setOnNodeChangedListener(listener)
    }

    private fun init() {
        binding = ViewLayoutInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val layoutParams = binding.layoutInfoContainer.layoutParams
        layoutParams.height = UiUtils.getDeviceHeight(context) / 2
        binding.layoutInfoContainer.layoutParams = layoutParams
        initViewpager()
    }

    private fun initViewpager() {
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}
