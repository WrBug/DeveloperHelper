package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.wrbug.developerhelper.R
import kotlinx.android.synthetic.main.view_layout_info.view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter


class LayoutInfoView : FrameLayout {

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        LayoutInflater.from(context).inflate(R.layout.view_layout_info, this)
        initIndicator()
    }

    private fun initIndicator() {
        val commonNavigator = CommonNavigator(context)
        val navigatorAdapter = NavigatorAdapter()
        navigatorAdapter.viewPager = viewPager
        commonNavigator.adapter = navigatorAdapter
        magicIndicatorView.navigator = commonNavigator
        ViewPagerHelper.bind(magicIndicatorView, viewPager)
    }


}
