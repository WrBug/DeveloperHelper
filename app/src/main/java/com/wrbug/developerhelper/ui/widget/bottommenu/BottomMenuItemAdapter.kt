package com.wrbug.developerhelper.ui.widget.bottommenu

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.UiUtils

class BottomMenuItemAdapter(val context: Context) : RecyclerView.Adapter<BottomMenuItemAdapter.ViewHolder>() {
    var list: Array<String> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var listener: OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tv = TextView(context)
        tv.setTextColor(context.resources.getColor(R.color.text_color_666666))
        tv.gravity = Gravity.CENTER
        val dp10 = UiUtils.dp2px(context, 10F)
        tv.setPadding(dp10, dp10, dp10, dp10)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tv.layoutParams = params
        return ViewHolder(tv)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv.text = list[position]
        holder.index = position
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    inner class ViewHolder(val tv: TextView) : RecyclerView.ViewHolder(tv) {
        var index = 0

        init {
            tv.setOnClickListener {
                listener?.onClick(index)
            }
        }

    }
}
