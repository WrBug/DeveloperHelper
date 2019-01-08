package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.wrbug.developerhelper.model.entity.HierarchyNode
import com.wrbug.developerhelper.ui.widget.boundsinfoview.BoundsInfoView
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.InfoAdapter
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.ItemInfo

class LayoutInfoViewPagerAdapter(
    val context: Context,
    private val hierarchyNode: HierarchyNode
) : PagerAdapter() {
    private val tabList = arrayListOf<String>()
    private val viewList = arrayListOf<View>()
    private val infoAdapter: InfoAdapter = InfoAdapter(context)

    init {
        initInfoTab()
        initLayoutTable()
    }

    private fun initLayoutTable() {
        tabList.add("Layout")

        val boundsInfoView = BoundsInfoView(context)
        boundsInfoView.bounds = hierarchyNode.screenBounds
        viewList.add(boundsInfoView)
    }

    private fun initInfoTab() {
        tabList.add("Info")
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val list = with(hierarchyNode) {
            val list = arrayListOf<ItemInfo>()
            list.add(ItemInfo("Package", packageName))
            list.add(ItemInfo("Widget", widget))
            list.add(
                ItemInfo(
                    "Id",
                    "${resourceId.replace(packageName, "app")}[${idHex?.replace("#", "0x") ?: "NO_ID"}]"
                )
            )
            if (!text.isEmpty()) {
                list.add(ItemInfo("Text", text))
            }
            list.add(ItemInfo("Enable", enabled))
            list.add(ItemInfo("Clickable", clickable))
            list.add(ItemInfo("Checkable", checkable))
            list.add(ItemInfo("Checked", checked))

            list.add(ItemInfo("Focusable", focusable))
            list.add(ItemInfo("Focused", focused))
            list.add(ItemInfo("LongClickable", longClickable))
            list.add(ItemInfo("Bounds", screenBounds ?: ""))
            list.add(ItemInfo("Password", password))
            list.add(ItemInfo("Selected", selected))
            list
        }

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