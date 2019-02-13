package com.wrbug.developerhelper.ui.widget.layoutinfoview

import com.wrbug.developerhelper.model.entity.HierarchyNode

class HierarchyGraphNode(val node: HierarchyNode) {
    var selected: Boolean = false
    var childSelected = false
        set(value) {
            field = value
            parent?.childSelected = value
        }
    var parent: HierarchyGraphNode? = null
}