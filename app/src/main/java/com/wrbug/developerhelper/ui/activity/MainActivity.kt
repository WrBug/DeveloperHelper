package com.wrbug.developerhelper.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.service.DeveloperHelperAccessibilityService
import kotlinx.android.synthetic.main.activity_main.*
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.activity.guide.GuideActivity


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv.setOnClickListener {
            startActivity(Intent(this, GuideActivity::class.java))
        }
    }

}
