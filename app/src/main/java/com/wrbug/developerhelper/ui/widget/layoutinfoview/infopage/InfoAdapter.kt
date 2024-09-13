package com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.wrbug.developerhelper.commonutil.ClipboardUtils
import com.wrbug.developerhelper.commonutil.print
import com.wrbug.developerhelper.util.setOnDoubleCheckClickListener
import com.wrbug.developerhelper.util.visible
import com.wrbug.developerhelper.databinding.ItemInfoLoadingBinding
import com.wrbug.developerhelper.databinding.ItemViewInfoBinding

class InfoAdapter(val context: Context, private val topItem: ItemInfo? = null) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private val list = arrayListOf<Any>()
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        if (p1 == VIEW_TYPE_LOADING) {
            return LoadingViewHolder(
                ItemInfoLoadingBinding.inflate(
                    LayoutInflater.from(p0.context), p0, false
                )
            )
        }
        return InfoViewHolder(
            ItemViewInfoBinding.inflate(
                LayoutInflater.from(p0.context), p0, false
            )
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] is LoadingItem) {
            VIEW_TYPE_LOADING
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(list: List<Any>) {
        this.list.clear()
        topItem?.let {
            this.list.add(it)
        }
        if (list.isEmpty().not()) {
            this.list.addAll(list)
        }
        notifyDataSetChanged()
    }

    fun addItem(index: Int, item: ItemInfo) {
        if (this.list.size < index) {
            return
        }
        list.add(index, item)
        notifyItemInserted(index)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is InfoViewHolder) {
            val itemInfo = list[position] as ItemInfo
            holder.binding.titleTv.text = itemInfo.title
            holder.binding.contentTv.text = itemInfo.content.toString()
            holder.binding.contentTv.setTextColor(itemInfo.textColor)
            holder.binding.contentTv.setTextIsSelectable(itemInfo.clickListener == null)
            holder.binding.ivCopy.visible = itemInfo.showCopy
            holder.binding.ivCopy.setOnDoubleCheckClickListener {
                ClipboardUtils.saveClipboardText(context, itemInfo.content.toString())
            }
            holder.binding.contentTv.setOnClickListener {
                itemInfo.content.print()
                itemInfo.clickListener?.run {
                    onClick(it)
                }
            }
        } else if (holder is LoadingViewHolder) {
            holder.binding.loadingView.visible = true
            holder.binding.loadingView.playAnimation()
            holder.binding.loadingView.repeatCount = LottieDrawable.INFINITE
        }
    }

    fun notifyItemChanged(enforceItem: ItemInfo) {
        val index = list.indexOf(enforceItem)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    class InfoViewHolder(val binding: ItemViewInfoBinding) : RecyclerView.ViewHolder(binding.root)
    class LoadingViewHolder(val binding: ItemInfoLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)
}