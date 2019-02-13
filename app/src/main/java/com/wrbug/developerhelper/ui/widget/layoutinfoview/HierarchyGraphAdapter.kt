package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import com.wrbug.developerhelper.R
import de.blox.graphview.BaseGraphAdapter

class HierarchyGraphAdapter(@NonNull val context: Context, @LayoutRes val layoutRes: Int) :
    BaseGraphAdapter<HierarchyGraphAdapter.ViewHolder>(context, layoutRes) {
    override fun onCreateViewHolder(view: View?) = ViewHolder(view!!)
    override fun onBindViewHolder(viewHolder: ViewHolder?, data: Any?, position: Int) {
        viewHolder?.run {
            val node = data as HierarchyGraphNode
            widgetTv.text = node.node.widget
            when {
                node.selected -> cardView.setCardBackgroundColor(context.resources.getColor(R.color.colorAccent))
                node.childSelected -> cardView.setCardBackgroundColor(context.resources.getColor(R.color.colorAccentLight))
                else -> cardView.setCardBackgroundColor(context.resources.getColor(R.color.colorPrimary))
            }
        }

    }


    class ViewHolder(itemView: View) {
        val widgetTv: TextView = itemView.findViewById(R.id.widgetTv)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }
}