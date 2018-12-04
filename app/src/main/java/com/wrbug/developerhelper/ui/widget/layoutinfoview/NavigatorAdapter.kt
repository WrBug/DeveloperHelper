package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.graphics.Color
import android.support.v4.view.ViewPager
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

class NavigatorAdapter : CommonNavigatorAdapter() {
    var viewPager: ViewPager? = null
    private val list = listOf("info", "layout")

    override fun getCount(): Int {
        return list.size
    }

    override fun getTitleView(context: Context, i: Int): IPagerTitleView? {
        val titleView = ColorTransitionPagerTitleView(context)
        titleView.normalColor = Color.GRAY
        titleView.selectedColor = Color.BLACK
        titleView.text = list[i]
        titleView.setOnClickListener {
            viewPager?.currentItem = i
        }
        return titleView
    }

    override fun getIndicator(context: Context): IPagerIndicator? {
        val linePagerIndicator = LinePagerIndicator(context)
        linePagerIndicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
        return linePagerIndicator
    }
}
