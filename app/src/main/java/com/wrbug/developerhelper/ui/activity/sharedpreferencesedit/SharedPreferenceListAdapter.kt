package com.wrbug.developerhelper.ui.activity.sharedpreferencesedit

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.model.entity.SharedPreferenceItemInfo

class SharedPreferenceListAdapter(val context: Context) :
    RecyclerView.Adapter<SharedPreferenceListAdapter.ViewHolder>() {
    private val data: ArrayList<SharedPreferenceItemInfo> = arrayListOf()
    private var onValueChangedListener: OnValueChangedListener? = null
    private var changedFlag: Long = 0

    fun setOnValueChangedListener(listener: OnValueChangedListener) {
        onValueChangedListener = listener
    }

    fun setData(array: Array<SharedPreferenceItemInfo>) {
        data.clear()
        changedFlag = 0
        data.addAll(array.sortedBy { it.key })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_shared_preference_info, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sharedPreferenceItemInfo = data[position]
        holder.titleTv?.text = sharedPreferenceItemInfo.key
        holder.contentTv?.setText(sharedPreferenceItemInfo.value)
        holder.restoreTv?.visibility =
                if (sharedPreferenceItemInfo.value == sharedPreferenceItemInfo.newValue) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
        if (sharedPreferenceItemInfo.isValueValid()) {
            holder.contentTv?.error = null
        } else {
            holder.contentTv?.error = context.getString(R.string.input_error)
        }
        holder.tag = sharedPreferenceItemInfo
        holder.addTextChangedListener()
    }

    fun getData(): Array<SharedPreferenceItemInfo> {
        return data.toTypedArray()
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTv: TextView? = itemView.findViewById(R.id.titleTv)
        val contentTv: EditText? = itemView.findViewById(R.id.contentEt)
        var restoreTv: TextView? = itemView.findViewById(R.id.restoreTv)
        var tag: SharedPreferenceItemInfo? = null
        var textWatcher: TextWatcher? = null

        init {
            restoreTv?.setOnClickListener {
                tag?.run {
                    contentTv?.setText(value)
                }
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
                                contentTv?.error = null
                            } else {
                                contentTv?.error = context.getString(R.string.input_error)
                            }
                        }
                        changedFlag = if (toString() == tag?.value) {
                            changedFlag and ((1L.shl(data.size + 1)) - 1 xor 1L.shl(index))
                        } else {
                            changedFlag or 1L shl index
                        }
                        onValueChangedListener?.onChanged(changedFlag != 0L)
                    }

                    restoreTv?.visibility =
                            if (tag?.value == tag?.newValue) {
                                View.INVISIBLE
                            } else {
                                View.VISIBLE
                            }
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            }
            contentTv?.addTextChangedListener(textWatcher)
        }
    }


    interface OnValueChangedListener {
        fun onChanged(changed: Boolean)
    }
}