package com.wrbug.developerhelper.ui.activity.xposed

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import kotlinx.android.synthetic.main.activity_shell_app_manager.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

abstract class BaseXposedAppManagerActivity : BaseActivity(),
    XposedAppListAdapter.OnItemChangedListener {
    private var tmpList = ArrayList<String>()
    private val adapter: XposedAppListAdapter by lazy {
        XposedAppListAdapter(this).apply {
            setOnItemChangedListener(this@BaseXposedAppManagerActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shell_app_manager)
        setupActionBar(R.id.toolbar) {
            title = getManagerTitle()
        }
        swipeLayout.setOnRefreshListener {
            getData()
        }
        initRv()
        getData()
    }

    private fun initRv() {
        shellAppRv.layoutManager = LinearLayoutManager(this)
        shellAppRv.adapter = adapter
    }

    abstract fun getManagerTitle(): String

    abstract fun getPackages(): List<String>

    private fun getData() {
        swipeLayout.isRefreshing = true
        doAsync {
            val list = getPackages()
            if (list.isEmpty()) {
                uiThread {
                    swipeLayout.isRefreshing = false
                    emptyView.visibility = View.VISIBLE
                    shellAppRv.visibility = View.INVISIBLE
                }
                tmpList.clear()
                return@doAsync
            }
            if (tmpList.size != list.size || !tmpList.containsAll(list)) {
                val data = ArrayList<ApkInfo>()
                list.forEach { it ->
                    AppInfoManager.getAppByPackageName(it)?.let {
                        data.add(it)
                    }
                }
                tmpList = ArrayList(list)
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
