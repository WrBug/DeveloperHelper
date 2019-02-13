package com.wrbug.developerhelper.ui.widget.layoutinfoview

import com.wrbug.developerhelper.model.entity.HierarchyNode

interface OnNodeChangedListener {
    fun onChanged(node: HierarchyNode, parentNode: HierarchyNode?)
}