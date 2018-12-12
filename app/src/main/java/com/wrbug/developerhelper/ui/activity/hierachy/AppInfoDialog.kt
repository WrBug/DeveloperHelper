package com.wrbug.developerhelper.ui.activity.hierachy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.model.entry.ApkInfo
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.InfoAdapter
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.ItemInfo
import kotlinx.android.synthetic.main.dialog_apk_info.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class AppInfoDialog : DialogFragment() {
    var apkInfo: ApkInfo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        arguments?.let {
            apkInfo = it.getParcelable("apkInfo")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_apk_info, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apkInfo?.let {
            logoIv.setImageDrawable(it.getIco())
            titleTv.text = it.getAppName()
            subTitleTv.text = it.applicationInfo.packageName
            val itemInfos = ArrayList<ItemInfo>()
            itemInfos.add(ItemInfo("VersionCode", it.packageInfo.versionCode))
            itemInfos.add(ItemInfo("VersionName", it.packageInfo.versionName))
            it.applicationInfo.className?.let {name->
                itemInfos.add(ItemInfo("Application", name))
            }
            itemInfos.add(ItemInfo("DataDir", it.applicationInfo.dataDir))
            itemInfos.add(ItemInfo("DataDir", it.applicationInfo.dataDir))
            activity?.let {
                val adapter = InfoAdapter(it)
                apkInfoRv.adapter = adapter
                apkInfoRv.layoutManager = LinearLayoutManager(it)
                adapter.setItems(itemInfos)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}