package com.wrbug.developerhelper.ui.activity.xposed.shellmanager

import android.os.Bundle
import android.view.View
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.xposed.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.xposed.processshare.ProcessDataManager
import kotlinx.android.synthetic.main.activity_shell_app_manager.*

class ShellAppManagerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell_app_manager)
        getShellApp()
    }

    private fun getShellApp() {
        val dexListProcessData = ProcessDataManager.get(DumpDexListProcessData::class.java)
        val packageNames = dexListProcessData.getData()
        packageNames?.apply {
            emptyView.visibility = if (isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
