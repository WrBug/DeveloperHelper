package com.wrbug.developerhelper.ui.activity.xposed.xposedsetting

import android.os.Bundle
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.ipc.processshare.manager.FileProcessDataManager
import com.wrbug.developerhelper.ui.activity.xposed.appxposedsetting.AppXposedSettingActivity
import com.wrbug.developerhelper.ui.activity.xposed.shellmanager.ShellAppManagerActivity
import kotlinx.android.synthetic.main.activity_xposed_setting.*
import java.io.File

class XposedSettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xposed_setting)
        setupActionBar(R.id.toolbar) {
            title = getString(R.string.xposed_setting)
        }
        appXposedSettingItemView.setOnClickListener {
            AppXposedSettingActivity.start(this)
        }
        shellSettingItemView.setOnClickListener {
            ShellAppManagerActivity.start(this)
        }
    }
}
