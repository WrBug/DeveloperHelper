package com.wrbug.developerhelper.ui.widget.hierarchyView

import com.wrbug.developerhelper.base.entry.HierarchyNode

class SelectedNodeInfo(var selectedNode: HierarchyNode, var parentNode: HierarchyNode?) {

    fun contains(nodeInfo: SelectedNodeInfo): Boolean {
        val bounds = nodeInfo.selectedNode.screenBounds ?: return false
        val screenBounds = selectedNode.screenBounds ?: return false
        return screenBounds.contains(bounds)
    }
}