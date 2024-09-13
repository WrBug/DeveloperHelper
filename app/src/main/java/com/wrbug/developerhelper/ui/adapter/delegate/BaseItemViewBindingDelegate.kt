package com.wrbug.developerhelper.ui.adapter.delegate

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.drakeet.multitype.ItemViewDelegate

abstract class BaseItemViewBindingDelegate<T, B : ViewBinding> :
    ItemViewDelegate<T, ViewBindingHolder<B>>() {

    final override fun onBindViewHolder(holder: ViewBindingHolder<B>, item: T) {
        onBindViewHolder(holder.binding, item)
    }

    final override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup
    ): ViewBindingHolder<B> {
        return ViewBindingHolder(onCreateViewBinding(LayoutInflater.from(context), parent))
    }

    abstract fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup): B

    abstract fun onBindViewHolder(binding: B, item: T)

}