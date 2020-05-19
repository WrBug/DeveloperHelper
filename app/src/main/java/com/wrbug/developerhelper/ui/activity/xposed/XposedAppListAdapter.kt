package com.wrbug.developerhelper.ui.activity.xposed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.ui.widget.bottommenu.BottomMenu
import com.wrbug.developerhelper.ui.widget.bottommenu.OnItemClickListener
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataCreator

class XposedAppListAdapter(val context: Context) :
    RecyclerView.Adapter<XposedAppListAdapter.ViewHolder>() {
    companion object {
        private val cache = ArrayList<ApkInfo>()
    }

    private val list = ArrayList<ApkInfo>()
    private val appEnableStatusMap = HashMap<String, Boolean>()
    private var listener: OnItemChangedListener? = null

    init {
        initApkInfo()
    }

    private fun initApkInfo(force: Boolean = false) {
        if (force || list.isEmpty()) {
            cache.clear()
            list.clear()
            cache.addAll(AppInfoManager.getAllApps().values)
            cache.sortBy { it.getAppName() }
            list.addAll(cache)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_shell_app_info,
                parent,
                false
            )
        )

    fun setAppEnableStatus(data: Map<String, Boolean>, force: Boolean = false) {
        initApkInfo(force)
        appEnableStatusMap.clear()
        appEnableStatusMap.putAll(data)
        list.sortBy { appEnableStatusMap[it.applicationInfo.packageName] != true }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
    fun setOnItemChangedListener(listener: OnItemChangedListener) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val apkInfo = list[position]
        holder.removeListener()
        holder.icoIv.setImageDrawable(apkInfo.getIco())
        holder.appNameTv.text = apkInfo.getAppName()
        holder.packageNameTv.text = apkInfo.packageInfo.packageName
        holder.toggle.isChecked = appEnableStatusMap[apkInfo.applicationInfo.packageName] == true
        holder.apkInfo = apkInfo
        holder.setListener()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icoIv: ImageView = itemView.findViewById(R.id.icoIv)
        var appNameTv: TextView = itemView.findViewById(R.id.appNameTv)
        var packageNameTv: TextView = itemView.findViewById(R.id.packageNameTv)
        var apkInfo: ApkInfo? = null
        val toggle: SwitchCompat = itemView.findViewById(R.id.toggle)

        fun removeListener() {
            toggle.setOnCheckedChangeListener(null)
        }

        fun setListener() {
            toggle.setOnCheckedChangeListener { _, isChecked ->
                apkInfo?.let {
                    listener?.onChanged(this@XposedAppListAdapter, it, isChecked)
                    appEnableStatusMap[it.applicationInfo.packageName] = isChecked
                }
            }
        }
    }

    interface OnItemChangedListener {
        fun onChanged(adapter: XposedAppListAdapter, apkInfo: ApkInfo, isChecked: Boolean)
    }
}
