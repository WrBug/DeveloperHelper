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
import com.wrbug.developerhelper.base.entry.HierarchyNode
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
    private var hierarchyNode: HierarchyNode
) : PagerAdapter() {
    private val tabList = arrayListOf<String>()
    private val viewList = arrayListOf<View>()
    private val infoAdapter: InfoAdapter = InfoAdapter(context)
    private lateinit var graphView: GraphView
    private val boundsInfoView = BoundsInfoView(context)
    private var onNodeChangedListener: ((HierarchyNode, HierarchyNode?) -> Unit)? = null

    init {
        initInfoTab()
        initLayoutTable()
        initViewTreeTab()
    }

    fun setOnNodeChangedListener(listener: (HierarchyNode, HierarchyNode?) -> Unit) {
        onNodeChangedListener = listener
    }

    private fun initViewTreeTab() {
        tabList.add("ViewTree")
        graphView =
            LayoutInflater.from(context).inflate(R.layout.layout_hierarchy_tree, null) as GraphView
        val adapter = ViewTreeGraphAdapter(context, R.layout.item_tree_node_view)
        graphView.adapter = adapter
        adapter.setOnItemClickListener(object : ViewTreeGraphAdapter.OnItemClickListener {
            override fun onClick(node: ViewTreeGraphNode, position: Int) {
                hierarchyNode = node.node
                resetInfoTab()
                resetLayoutTable()
                resetViewTreeTab(node)
                onNodeChangedListener?.invoke(node.node, node.parent?.node)
            }
        })
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

    private fun resetViewTreeTab(node: ViewTreeGraphNode) {
        graphView.adapter.graph = graphView.adapter.graph?.apply {
            for (edge in nodes) {
                (edge?.data as ViewTreeGraphNode?)?.apply {
                    selected = this == node
                    childSelected = false
                    if (selected) {
                        parent?.childSelected = true
                    }
                }
            }
        }

    }

    private fun resetInfoTab() {
        infoAdapter.setItems(setInfo())
    }

    private fun resetLayoutTable() {
        boundsInfoView.bounds = hierarchyNode.screenBounds
    }

    private fun initGraph(nodeList: List<HierarchyNode>?, parentNode: Node?, graph: Graph) {
        nodeList?.run {
            val map = LinkedHashMap<HierarchyNode, Node>()
            for (hNode in this) {
                val graphNode = ViewTreeGraphNode(hNode)
                val node = Node(graphNode)
                if (hNode == hierarchyNode) {
                    graphNode.selected = true
                }
                parentNode?.let {
                    graph.addEdge(parentNode, node)
                    graphNode.parent = (it.data as ViewTreeGraphNode?)?.apply {
                        if (graphNode.selected) {
                            childSelected = true
                        }
                    }
                }
                map[hNode] = node
            }
            for ((k, v) in map) {
                initGraph(k.childId, v, graph)
            }
        }
    }

    private fun initLayoutTable() {
        tabList.add("Layout")
        boundsInfoView.bounds = hierarchyNode.screenBounds
        viewList.add(boundsInfoView)
    }

    private fun initInfoTab() {
        tabList.add("Info")
        val recyclerView = RecyclerView(context)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val list = setInfo()
        recyclerView.adapter = infoAdapter
        val params = ViewPager.LayoutParams()
        infoAdapter.setItems(list)
        recyclerView.layoutParams = params
        viewList.add(recyclerView)
    }

    private fun setInfo() = with(hierarchyNode) {
        val list = arrayListOf<ItemInfo>()
        list.add(ItemInfo("Package", packageName))
        list.add(ItemInfo("Widget", widget))
        list.add(
            ItemInfo(
                "Id",
                "${resourceId.replace(packageName, "app")}[${idHex?.replace("#", "0x") ?: "NO_ID"}]"
            )
        )
        if (text.isNotEmpty()) {
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