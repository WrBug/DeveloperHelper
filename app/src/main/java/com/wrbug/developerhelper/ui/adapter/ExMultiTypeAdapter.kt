package com.wrbug.developerhelper.ui.adapter

import com.drakeet.multitype.MultiTypeAdapter
import com.wrbug.developerhelper.ui.adapter.bean.EmptyViewItemData
import com.wrbug.developerhelper.ui.adapter.bean.LoadingViewItemData
import com.wrbug.developerhelper.ui.adapter.delegate.EmptyViewItemDelegate
import com.wrbug.developerhelper.ui.adapter.delegate.LoadingItemDelegate

class ExMultiTypeAdapter : MultiTypeAdapter() {

    companion object {
        fun get() = lazy {
            ExMultiTypeAdapter()
        }
    }

    init {
        register(EmptyViewItemDelegate())
        register(LoadingItemDelegate())
    }

    fun showEmpty(title: String = "") {
        items = listOf(EmptyViewItemData(title))
        notifyDataSetChanged()
    }

    fun showLoading() {
        items = listOf(LoadingViewItemData)
        notifyDataSetChanged()
    }

    fun loadData(list: List<Any>) {
        items = list.toList()
        notifyDataSetChanged()
    }
}