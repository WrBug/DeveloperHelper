package com.wrbug.developerhelper.ui.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.wrbug.developerhelper.databinding.ItemLoadingDataBinding
import com.wrbug.developerhelper.ui.adapter.bean.LoadingViewItemData

class LoadingItemDelegate :
    BaseItemViewBindingDelegate<LoadingViewItemData, ItemLoadingDataBinding>() {
    override fun onBindViewHolder(binding: ItemLoadingDataBinding, item: LoadingViewItemData) {

    }

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemLoadingDataBinding {
        return ItemLoadingDataBinding.inflate(inflater, parent, false)
    }
}