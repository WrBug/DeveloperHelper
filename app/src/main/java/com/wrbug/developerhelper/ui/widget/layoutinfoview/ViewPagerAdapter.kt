package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.wrbug.developerhelper.model.entry.HierarchyNode
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.InfoAdapter
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.ItemInfo

class ViewPagerAdapter(
    val context: Context,
    private val hierarchyNode: HierarchyNode
) : PagerAdapter() {
    private val tabList = arrayListOf<String>()
    private val viewList = arrayListOf<View>()
    private val infoAdapter: InfoAdapter = InfoAdapter(context)

    init {
        initInfoTab()
//        initLayoutTable()
    }

    private fun initLayoutTable() {
        tabList.add("Layout")
    }

    private fun initInfoTab() {
        tabList.add("Info")
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val list = arrayListOf<ItemInfo>()
        list.add(ItemInfo("Package", hierarchyNode.packagePath))
        list.add(ItemInfo("Widget", hierarchyNode.widget))
        list.add(ItemInfo("Enable", hierarchyNode.enabled))
        list.add(ItemInfo("Clickable", hierarchyNode.clickable))
        list.add(ItemInfo("Checkable", hierarchyNode.checkable))
        list.add(ItemInfo("Checked", hierarchyNode.checked))
        list.add(ItemInfo("Text", hierarchyNode.text))
        list.add(ItemInfo("Focusable", hierarchyNode.focusable))
        list.add(ItemInfo("Focused", hierarchyNode.focused))
        list.add(ItemInfo("LongClickable", hierarchyNode.longClickable))
        list.add(ItemInfo("ScreenBounds", hierarchyNode.screenBounds ?: ""))
        list.add(ItemInfo("ParentBounds", hierarchyNode.parentBounds ?: ""))
        list.add(ItemInfo("Password", hierarchyNode.password))
        list.add(ItemInfo("Selected", hierarchyNode.selected))
        recyclerView.adapter = infoAdapter
        val params = ViewPager.LayoutParams()
        infoAdapter.setItems(list)
        recyclerView.layoutParams = params
        viewList.add(recyclerView)
    }


    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 == p1
    }

    override fun getCount(): Int {
        return tabList.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewList[position])
        return viewList[position]

    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabList[position]
    }
}