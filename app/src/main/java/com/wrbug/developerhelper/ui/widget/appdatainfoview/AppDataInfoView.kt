package com.wrbug.developerhelper.ui.widget.appdatainfoview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.model.entity.ApkInfo
import com.wrbug.developerhelper.util.ApkUtils
import kotlinx.android.synthetic.main.view_app_data_info.view.*

class AppDataInfoView : FrameLayout {
    var apkInfo: ApkInfo? = null
        set(value) {
            field = value
            loadData()
        }

    constructor(context: Context) : super(context) {
        initView()

    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_app_data_info, this)
    }

    private fun loadData() {
        apkInfo?.run {
            apkPathTv.text = applicationInfo.publicSourceDir
            val apkSignInfo = ApkUtils.getApkSignInfo(context, applicationInfo.packageName)
            apkSha1Tv.text = apkSignInfo.sha1
            apkMd5Tv.text = apkSignInfo.md5
        }
    }
}