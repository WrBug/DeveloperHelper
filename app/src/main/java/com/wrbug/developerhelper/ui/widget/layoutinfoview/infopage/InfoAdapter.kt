package com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wrbug.developerhelper.commonutil.print
import com.wrbug.developerhelper.databinding.ItemViewInfoBinding

class InfoAdapter(val context: Context): RecyclerView.Adapter<InfoAdapter.ViewHolder>() {

    private val list = arrayListOf<ItemInfo>()
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(ItemViewInfoBinding.inflate(LayoutInflater.from(p0.context), p0, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItems(list: ArrayList<ItemInfo>) {
        this.list.clear()
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

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val itemInfo = list[p1]
        p0.binding.titleTv.text = itemInfo.title
        p0.binding.contentTv.text = itemInfo.content.toString()
        p0.binding.contentTv.setTextColor(itemInfo.textColor)
        p0.binding.contentTv.setTextIsSelectable(itemInfo.clickListener == null)
        p0.binding.contentTv.setOnClickListener {
            itemInfo.content.print()
            itemInfo.clickListener?.run {
                onClick(it)
            }
        }
    }

    fun notifyItemChanged(enforceItem: ItemInfo) {
        val index = list.indexOf(enforceItem)
        if (index >= 0) {
            notifyItemChanged(index)
        }
    }

    class ViewHolder(val binding: ItemViewInfoBinding): RecyclerView.ViewHolder(binding.root) {

    }
}