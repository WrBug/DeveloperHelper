package com.wrbug.developerhelper.ui.activity

import android.content.Intent
import android.os.Bundle
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.activity.guide.GuideActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FloatWindowService.start(this)
        tv.setOnClickListener {
            startActivity(Intent(this, GuideActivity::class.java))
        }
    }

}
