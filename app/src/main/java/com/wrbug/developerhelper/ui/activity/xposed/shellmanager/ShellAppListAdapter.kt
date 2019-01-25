package com.wrbug.developerhelper.ui.activity.xposed.shellmanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.ui.widget.bottommenu.BottomMenu
import com.wrbug.developerhelper.ui.widget.bottommenu.OnItemClickListener
import com.wrbug.developerhelper.xposed.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.xposed.processshare.ProcessDataManager

class ShellAppListAdapter(val context: Context) : RecyclerView.Adapter<ShellAppListAdapter.ViewHolder>() {
    private val list = ArrayList<ApkInfo>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_shell_app_info, parent, false))

    fun setData(data: List<ApkInfo>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apkInfo = list[position]
        holder.icoIv.setImageDrawable(apkInfo.getIco())
        holder.appNameTv.text = apkInfo.getAppName()
        holder.packageNameTv.text = apkInfo.packageInfo.packageName
        holder.apkInfo = apkInfo
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icoIv: ImageView = itemView.findViewById(R.id.icoIv)
        var appNameTv: TextView = itemView.findViewById(R.id.appNameTv)
        var packageNameTv: TextView = itemView.findViewById(R.id.packageNameTv)
        var apkInfo: ApkInfo? = null

        init {
            itemView.setOnClickListener {
                apkInfo?.apply {
                    showDialog(this)
                }
            }
        }

        private fun showDialog(apkInfo: ApkInfo) {
            val bottomMenu = BottomMenu.Builder(context)
                .menuItems(arrayOf(context.getString(R.string.remove_item)))
                .onItemClickListener(object : OnItemClickListener {
                    override fun onClick(position: Int) {
                        when (position) {
                            0 -> {
                                removeItem(apkInfo)
                            }
                        }
                    }
                })
                .build()
            bottomMenu.show()
        }
    }

    private fun removeItem(apkInfo: ApkInfo) {
        val dexListProcessData = ProcessDataManager.get(DumpDexListProcessData::class.java)
        val packageNames = dexListProcessData.getData()
        packageNames?.apply {
            remove(apkInfo.packageInfo.packageName)
            dexListProcessData.setData(this)
        }
        val index = list.indexOf(apkInfo)
        list.remove(apkInfo)
        notifyItemRemoved(index)
    }

}
