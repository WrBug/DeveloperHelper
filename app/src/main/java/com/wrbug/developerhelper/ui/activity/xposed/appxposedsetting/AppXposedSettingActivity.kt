package com.wrbug.developerhelper.ui.activity.xposed.appxposedsetting

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.ipc.processshare.manager.AppXposedProcessDataManager
import com.wrbug.developerhelper.ipc.processshare.manager.DumpDexListProcessDataManager
import com.wrbug.developerhelper.ui.activity.xposed.BaseXposedAppManagerActivity
import com.wrbug.developerhelper.ui.activity.xposed.XposedAppListAdapter
import com.wrbug.developerhelper.ui.activity.xposed.shellmanager.ShellAppManagerActivity

class AppXposedSettingActivity : BaseXposedAppManagerActivity() {

    override fun getManagerTitle(): String = getString(R.string.app_xposed_function_manager)

    override fun getAppEnableStatus(): Map<String, Boolean> {
        val packageNames = AppXposedProcessDataManager.instance.getAppXposedStatusList()
        return packageNames
    }

    override fun onChanged(adapter: XposedAppListAdapter, apkInfo: ApkInfo, isChecked: Boolean) {
        AppXposedProcessDataManager.instance.setAppXposedStatusList(apkInfo.applicationInfo.packageName to isChecked)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, AppXposedSettingActivity::class.java))
        }
    }
}
