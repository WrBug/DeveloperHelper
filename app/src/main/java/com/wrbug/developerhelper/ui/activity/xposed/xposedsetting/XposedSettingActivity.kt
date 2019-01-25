package com.wrbug.developerhelper.ui.activity.xposed.xposedsetting

import android.os.Bundle
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.basecommon.setupActionBar
import kotlinx.android.synthetic.main.activity_xposed_setting.*

class XposedSettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xposed_setting)
        setupActionBar(R.id.toolbar) {
            title = getString(R.string.xposed_setting)
        }
        shellSettingItemView.setOnClickListener {

        }
    }
}
