package com.wrbug.developerhelper.ui.activity.databaseedit

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.evrencoskun.tableview.adapter.AbstractTableAdapter
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder
import android.widget.TextView
import com.wrbug.developerhelper.R
import android.view.LayoutInflater
import android.widget.LinearLayout


class DatabaseTableAdapter(val context: Context) : AbstractTableAdapter<String, Int, String?>(context) {
    override fun onCreateColumnHeaderViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder {
        val layout = LayoutInflater.from(context).inflate(
            R.layout
                .table_view_column_header_layout, parent, false
        )
        return ColumnHeaderViewHolder(layout)
    }

    override fun onBindColumnHeaderViewHolder(
        holder: AbstractViewHolder?,
        columnHeaderItemModel: Any?,
        columnPosition: Int
    ) {
        val viewHolder = holder as ColumnHeaderViewHolder?
        viewHolder?.run {
            cellText?.text = columnHeaderItemModel?.toString()
        }
    }


    override fun onCreateRowHeaderViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder {
        val layout = LayoutInflater.from(context).inflate(
            R.layout.table_view_row_header_layout, parent, false
        )
        return RowHeaderViewHolder(layout)
    }

    override fun onBindRowHeaderViewHolder(holder: AbstractViewHolder?, rowHeaderItemModel: Any?, rowPosition: Int) {
        val viewHolder = holder as RowHeaderViewHolder?
        viewHolder?.run {
            cellText?.text = rowHeaderItemModel?.toString()
        }
    }


    override fun onCreateCellViewHolder(parent: ViewGroup?, viewType: Int): AbstractViewHolder {
        val layout = LayoutInflater.from(context).inflate(
            R.layout.table_view_cell_layout,
            parent, false
        )
        return CellViewHolder(layout)
    }

    @SuppressLint("InflateParams")
    override fun onCreateCornerView(): View {
        return LayoutInflater.from(context).inflate(R.layout.table_view_corner_layout, null)
    }

    override fun onBindCellViewHolder(
        holder: AbstractViewHolder?,
        cellItemModel: Any?,
        columnPosition: Int,
        rowPosition: Int
    ) {
        val viewHolder = holder as CellViewHolder?
        viewHolder?.run {
            cellText?.text = cellItemModel?.toString() ?: "NULL"
            itemView.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            cellText?.requestLayout()
        }
    }

    override fun getColumnHeaderItemViewType(position: Int): Int {
        return 0
    }

    override fun getRowHeaderItemViewType(position: Int): Int {
        return 0
    }

    override fun getCellItemViewType(position: Int): Int {
        return 0
    }

    internal inner class ColumnHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {
        val cellText: TextView? = itemView.findViewById(R.id.column_header_textView)
    }


    internal inner class RowHeaderViewHolder(itemView: View) : AbstractViewHolder(itemView) {

        val cellText: TextView? = itemView.findViewById(R.id.row_header_textview)
    }

    internal inner class CellViewHolder(itemView: View) : AbstractViewHolder(itemView) {

        val cellText: TextView? = itemView.findViewById(R.id.cell_data)

    }
}