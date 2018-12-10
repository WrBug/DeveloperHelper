package com.wrbug.developerhelper.ui.activity

import android.os.Bundle
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.service.FloatWindowService
import kotlinx.android.synthetic.main.layout_toolbar.*


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FloatWindowService.start(this)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }

    override fun onResume() {
        super.onResume()

    }
}
