package com.wrbug.developerhelper.ui.widget.hierarchyView

import android.graphics.Rect
import com.wrbug.developerhelper.model.entity.HierarchyNode

class SelectedNodeInfo(var selectedNode: HierarchyNode, var parentNode: HierarchyNode?) {

    fun contains(nodeInfo: SelectedNodeInfo): Boolean {
        val bounds = nodeInfo.selectedNode.screenBounds ?: return false
        val screenBounds = selectedNode.screenBounds ?: return false
        return screenBounds.contains(bounds)
    }
}