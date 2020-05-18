package com.wrbug.developerhelper.ui.activity.xposed.shellmanager

import android.content.Context
import android.content.Intent
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.ipc.processshare.manager.DumpDexListProcessDataManager
import com.wrbug.developerhelper.ui.activity.xposed.BaseXposedAppManagerActivity
import com.wrbug.developerhelper.ui.activity.xposed.XposedAppListAdapter

class ShellAppManagerActivity : BaseXposedAppManagerActivity() {
    override fun getManagerTitle(): String = getString(R.string.shell_app_manager)

    override fun getAppEnableStatus(): Map<String, Boolean> {
        val packageNames = DumpDexListProcessDataManager.instance.getData()
        return packageNames
    }

    override fun onChanged(adapter: XposedAppListAdapter, apkInfo: ApkInfo, isChecked: Boolean) {
        DumpDexListProcessDataManager.instance.setData(apkInfo.applicationInfo.packageName to isChecked)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ShellAppManagerActivity::class.java))
        }
    }

}
