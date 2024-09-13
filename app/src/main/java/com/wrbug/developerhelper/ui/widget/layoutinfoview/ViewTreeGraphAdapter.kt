package com.wrbug.developerhelper.ui.widget.layoutinfoview

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import com.wrbug.developerhelper.R
import de.blox.graphview.BaseGraphAdapter

class ViewTreeGraphAdapter(val context: Context, @LayoutRes val layoutRes: Int) :
    BaseGraphAdapter<ViewTreeGraphAdapter.ViewHolder>(context, layoutRes) {
    private var listener: OnItemClickListener? = null
    override fun onCreateViewHolder(view: View?) = ViewHolder(view!!)
    override fun onBindViewHolder(viewHolder: ViewHolder?, data: Any?, position: Int) {
        viewHolder?.run {
            val node = data as ViewTreeGraphNode
            widgetTv.text = node.node.widget
            cardView.setOnClickListener {
                widgetTv.text = node.node.widget
                listener?.onClick(node, position)
            }
            when {
                node.selected -> cardView.setCardBackgroundColor(context.resources.getColor(R.color.colorAccent))
                node.childSelected -> cardView.setCardBackgroundColor(context.resources.getColor(R.color.colorAccentLight))
                else -> {
                    cardView.setCardBackgroundColor(context.resources.getColor(R.color.colorPrimary))
                    if (node.shortName) {
                        widgetTv.text = toShortName(node.node.widget)
                    } else {
                        widgetTv.text = node.node.widget
                    }

                }
            }
        }

    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        listener = onItemClickListener
    }

    private fun toShortName(name: String): String {
        if (name.contains(".").not()) {
            return name
        }
        val splitName = name.split(".")
        if (splitName.isNotEmpty()) {
            val realName = splitName[splitName.size - 1]
            if (realName.length > 4) {
                return realName.substring(0, 4) + "..."
            }
            return realName
        }
        return name
    }

    class ViewHolder(itemView: View) {
        val widgetTv: TextView = itemView.findViewById(R.id.widgetTv)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    interface OnItemClickListener {
        fun onClick(node: ViewTreeGraphNode, position: Int)
    }
}