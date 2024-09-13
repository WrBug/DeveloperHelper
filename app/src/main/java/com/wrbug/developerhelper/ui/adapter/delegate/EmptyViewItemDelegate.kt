package com.wrbug.developerhelper.ui.adapter.delegate

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.databinding.ItemEmptyDataBinding
import com.wrbug.developerhelper.ui.adapter.bean.EmptyViewItemData
import com.wrbug.developerhelper.util.getString

class EmptyViewItemDelegate :
    BaseItemViewBindingDelegate<EmptyViewItemData, ItemEmptyDataBinding>() {
    override fun onBindViewHolder(binding: ItemEmptyDataBinding, item: EmptyViewItemData) {
        if (item.title.isNotEmpty()) {
            binding.emptyView.setTitle(item.title)
        } else {
            binding.emptyView.setTitle(R.string.no_data.getString())
        }
    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemEmptyDataBinding {
        return ItemEmptyDataBinding.inflate(inflater, parent, false)
    }
}