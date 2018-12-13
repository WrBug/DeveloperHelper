package com.wrbug.developerhelper.ui.activity.hierachy

import android.app.Activity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.model.entry.ApkInfo
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.InfoAdapter
import com.wrbug.developerhelper.ui.widget.layoutinfoview.infopage.ItemInfo
import com.wrbug.developerhelper.util.UiUtils
import com.wrbug.developerhelper.util.formatyyyyMMddHHmmss
import kotlinx.android.synthetic.main.dialog_apk_info.*
import java.util.*

class AppInfoDialog : DialogFragment() {
    var apkInfo: ApkInfo? = null
    var listener: AppInfoDialogEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog)
        arguments?.let {
            apkInfo = it.getParcelable("apkInfo")
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (activity is AppInfoDialogEventListener) {
            listener = activity
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_apk_info, container, false)
        dialog.window.takeUnless {
            it == null
        }?.run {
            val layoutParams = attributes
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = UiUtils.getDeviceHeight() / 2
            attributes = layoutParams
            setGravity(Gravity.TOP)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleContainer.setPadding(0, UiUtils.getStatusHeight(), 0, 0)
        apkInfo?.let {
            logoIv.setImageDrawable(it.getIco())
            titleTv.text = it.getAppName()
            titleTv.setOnClickListener {
                listener?.showHierachyView()
                dismissAllowingStateLoss()
            }
            subTitleTv.text = it.applicationInfo.packageName
            val itemInfos = ArrayList<ItemInfo>()
            itemInfos.add(ItemInfo("VersionCode", it.packageInfo.versionCode))
            itemInfos.add(ItemInfo("VersionName", it.packageInfo.versionName))
            it.applicationInfo.className?.let { name ->
                itemInfos.add(ItemInfo("Application", name))
            }
            itemInfos.add(ItemInfo("uid", it.applicationInfo.uid))
            itemInfos.add(
                ItemInfo(
                    "首次安装时间",
                    it.packageInfo.firstInstallTime.formatyyyyMMddHHmmss()
                )
            )
            itemInfos.add(
                ItemInfo(
                    "最后更新时间",
                    it.packageInfo.lastUpdateTime.formatyyyyMMddHHmmss()
                )
            )
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
        listener?.close()
    }
}