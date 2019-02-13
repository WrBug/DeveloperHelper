package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.model.entity.HierarchyNode
import com.wrbug.developerhelper.ui.widget.boundsinfoview.BoundsInfoView
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.InfoAdapter
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.ItemInfo
import de.blox.graphview.*
import de.blox.graphview.tree.BuchheimWalkerAlgorithm
import de.blox.graphview.tree.BuchheimWalkerConfiguration
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class LayoutInfoViewPagerAdapter(
    val context: Context,
    private val nodeList: List<HierarchyNode>?,
    private val hierarchyNode: HierarchyNode
) : PagerAdapter() {

    private val tabList = arrayListOf<String>()
    private val viewList = arrayListOf<View>()
    private val infoAdapter: InfoAdapter = InfoAdapter(context)
    private lateinit var graphView: GraphView

    init {
        initInfoTab()
        initLayoutTable()
        initHierarchyTab()
    }

    private fun initHierarchyTab() {
        tabList.add("Hierarchy")
        graphView =
                LayoutInflater.from(context).inflate(R.layout.layout_hierarchy_tree, null) as GraphView
        val adapter = HierarchyGraphAdapter(context, R.layout.item_tree_node_view)
        graphView.adapter = adapter
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(300)
            .setSubtreeSeparation(300)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        adapter.algorithm = BuchheimWalkerAlgorithm(configuration)
        viewList.add(graphView)
        doAsync {
            val graph = Graph()
            initGraph(nodeList, null, graph)
            uiThread {
                adapter.graph = graph
            }
        }
    }


    private fun initGraph(nodeList: List<HierarchyNode>?, parentNode: Node?, graph: Graph) {
        nodeList?.run {
            for (hNode in this) {
                val graphNode = HierarchyGraphNode(hNode)
                val node = Node(graphNode)
                if (hNode == hierarchyNode) {
                    graphNode.selected = true
                }
                parentNode?.let {
                    graph.addEdge(parentNode, node)
                    graphNode.parent = (it.data as HierarchyGraphNode?)?.apply {
                        if (graphNode.selected) {
                            childSelected = true
                        }
                    }
                }
                initGraph(hNode.childId, node, graph)
            }
        }
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