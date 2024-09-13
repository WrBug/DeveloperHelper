package com.wrbug.developerhelper.ui.widget.layoutinfoview

import com.wrbug.developerhelper.base.entry.HierarchyNode

interface OnNodeChangedListener {
    fun onChanged(node: HierarchyNode, parentNode: HierarchyNode?)
}