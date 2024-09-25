package com.wrbug.developerhelper.ui.activity.sharedpreferencesedit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.util.inVisible
import com.wrbug.developerhelper.databinding.ItemSharedPreferenceInfoBinding
import com.wrbug.developerhelper.model.entity.SharedPreferenceItemInfo

class SharedPreferenceListAdapter(val context: Context) :
    RecyclerView.Adapter<SharedPreferenceListAdapter.ViewHolder>() {
    private val data: ArrayList<SharedPreferenceItemInfo> = arrayListOf()
    private var onValueChangedListener: ((Boolean) -> Unit)? = null
    private var changedFlag: Long = 0

    fun setOnValueChangedListener(listener: (Boolean) -> Unit) {
        onValueChangedListener = listener
    }

    fun setData(array: Array<SharedPreferenceItemInfo>) {
        data.clear()
        changedFlag = 0
        data.addAll(array.sortedBy { it.key.lowercase() })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSharedPreferenceInfoBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sharedPreferenceItemInfo = data[position]
        holder.removeTextChangedListener()
        holder.binding.titleTv.text = sharedPreferenceItemInfo.key
        holder.binding.contentEt.setText(sharedPreferenceItemInfo.newValue)
        holder.binding.restoreTv.inVisible =
            sharedPreferenceItemInfo.value == sharedPreferenceItemInfo.newValue
        if (sharedPreferenceItemInfo.isValueValid()) {
            holder.binding.contentEt.error = null
        } else {
            holder.binding.contentEt.error = context.getString(R.string.input_error)
        }
        holder.tag = sharedPreferenceItemInfo
        holder.addTextChangedListener()
    }

    fun getData(): Array<SharedPreferenceItemInfo> {
        return data.toTypedArray()
    }


    inner class ViewHolder(val binding: ItemSharedPreferenceInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var tag: SharedPreferenceItemInfo? = null
        var textWatcher: TextWatcher? = null

        init {
            binding.restoreTv.setOnClickListener {
                tag?.run {
                    binding.contentEt.setText(value)
                }
            }
        }

        fun removeTextChangedListener() {
            if (textWatcher != null) {
                binding.contentEt.removeTextChangedListener(textWatcher)
                textWatcher = null
            }
        }

        fun addTextChangedListener() {
            if (textWatcher != null) {
                return
            }
            textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val index = data.indexOf(tag)
                    if (index < 0) {
                        return
                    }
                    s.run {
                        tag?.let {
                            it.newValue = toString()
                            if (it.isValueValid()) {
                                binding.contentEt.error = null
                            } else {
                                binding.contentEt.error = context.getString(R.string.input_error)
                            }
                        }
                        changedFlag = if (toString() == tag?.value) {
                            changedFlag and ((1L.shl(data.size + 1)) - 1 xor 1L.shl(index))
                        } else {
                            changedFlag or 1L shl index
                        }
                        onValueChangedListener?.invoke(changedFlag != 0L)
                    }

                    binding.restoreTv.inVisible = tag?.value == tag?.newValue
                }

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            }
            binding.contentEt.addTextChangedListener(textWatcher)
        }
    }
}