package com.wrbug.developerhelper.ui.adapter.delegate

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class ViewBindingHolder<T : ViewBinding>(val binding: T) :
    RecyclerView.ViewHolder(binding.root)