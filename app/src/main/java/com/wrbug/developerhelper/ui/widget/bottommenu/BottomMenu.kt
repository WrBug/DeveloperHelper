package com.wrbug.developerhelper.ui.widget.bottommenu

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.databinding.DialogBottomMenuBinding
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration

class BottomMenu(context: Context): BottomSheetDialog(context), OnItemClickListener {

    private var title = ""
    val adapter: BottomMenuItemAdapter by lazy {
        BottomMenuItemAdapter(context)
    }
    private lateinit var binding: DialogBottomMenuBinding
    private var items: Array<String> = arrayOf()
    private var listener: OnItemClickListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogBottomMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRv()
        initView()
    }

    private fun initView() {
        binding.titleTv.text = title
        binding.titleTv.visibility = if (title.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun initRv() {
        binding.menuListRv.layoutManager = LinearLayoutManager(context)
        val decoration = SpaceItemDecoration(UiUtils.dp2px(context, 0F))
        decoration.setLastBottomPadding(UiUtils.dp2px(context, 10F))
        decoration.setFirstTopPadding(UiUtils.dp2px(context, 10F))
        binding.menuListRv.addItemDecoration(decoration)
        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ColorDrawable(context.resources.getColor(R.color.divider_color)))
        binding.menuListRv.addItemDecoration(dividerItemDecoration)
        adapter.setOnItemClickListener(this)
        binding.menuListRv.adapter = adapter
        adapter.list = items
    }

    override fun onClick(position: Int) {
        listener?.onClick(position)
        dismiss()
    }

    class Builder(val context: Context) {

        private var title = ""
        private var items: Array<String> = arrayOf()
        private var listener: OnItemClickListener? = null

        fun title(id: Int): Builder {
            title = context.getString(id)
            return this
        }

        fun title(title: String): Builder {
            this.title = title
            return this
        }

        fun menuItems(items: Array<String>): Builder {
            this.items = items
            return this
        }

        fun onItemClickListener(listener: OnItemClickListener?): Builder {
            this.listener = listener
            return this
        }

        fun build(): BottomMenu {
            val menu = BottomMenu(context)
            menu.items = items
            menu.title = title
            menu.listener = listener
            return menu
        }
    }

}
