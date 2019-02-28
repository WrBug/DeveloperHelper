package com.wrbug.developerhelper.ui.activity.xposed.shellmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataManager
import kotlinx.android.synthetic.main.activity_shell_app_manager.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class ShellAppManagerActivity : BaseActivity() {
    private var tmpList = ArrayList<String>()
    private val adapter: ShellAppListAdapter by lazy {
        ShellAppListAdapter(this)
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ShellAppManagerActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell_app_manager)
        setupActionBar(R.id.toolbar) {
            title = getString(R.string.shell_app_manager)
        }
        swipeLayout.setOnRefreshListener {
            getShellApp()
        }
        initRv()
        getShellApp()
    }

    private fun initRv() {
        shellAppRv.layoutManager = LinearLayoutManager(this)
        shellAppRv.adapter = adapter
    }

    private fun getShellApp() {
        swipeLayout.isRefreshing = true
        doAsync {
            val dexListProcessData = ProcessDataManager.get(DumpDexListProcessData::class.java)
            val packageNames = dexListProcessData.getData()
            if (packageNames == null || packageNames.isEmpty()) {
                uiThread {
                    swipeLayout.isRefreshing = false
                    emptyView.visibility = View.VISIBLE
                    shellAppRv.visibility = View.INVISIBLE
                }
                tmpList.clear()
                return@doAsync
            }
            if (tmpList.size != packageNames.size || !tmpList.containsAll(packageNames)) {
                val data = ArrayList<ApkInfo>()
                packageNames.forEach { it ->
                    AppInfoManager.getAppByPackageName(it)?.let {
                        data.add(it)
                    }
                }
                tmpList = packageNames
                uiThread {
                    setData(data)
                }
            } else {
                uiThread {
                    swipeLayout.isRefreshing = false
                }
            }
        }
    }

    private fun setData(list: List<ApkInfo>) {
        swipeLayout.isRefreshing = false
        emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        shellAppRv.visibility = if (list.isEmpty()) View.INVISIBLE else View.VISIBLE
        if (list.isEmpty().not()) {
            adapter.setData(list)
        }
    }
}
