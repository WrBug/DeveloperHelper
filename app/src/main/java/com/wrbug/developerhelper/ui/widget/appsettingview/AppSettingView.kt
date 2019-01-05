package com.wrbug.developerhelper.ui.widget.appsettingview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ScrollView
import com.wrbug.developerhelper.R

class AppSettingView : ScrollView {
    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_app_setting, this)
    }
}