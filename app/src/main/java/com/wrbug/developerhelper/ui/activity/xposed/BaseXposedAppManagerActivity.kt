package com.wrbug.developerhelper.ui.activity.xposed

import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.basecommon.setupActionBar
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.ui.decoration.SpaceItemDecoration
import kotlinx.android.synthetic.main.activity_base_xposed_app_manager.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

abstract class BaseXposedAppManagerActivity : BaseActivity(),
    XposedAppListAdapter.OnItemChangedListener {
    private val adapter: XposedAppListAdapter by lazy {
        XposedAppListAdapter(this).apply {
            setOnItemChangedListener(this@BaseXposedAppManagerActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_xposed_app_manager)
        setupActionBar(R.id.toolbar) {
            title = getManagerTitle()
        }
        swipeLayout.setOnRefreshListener {
            getData(true)
        }
        initRv()
        getData()
    }

    private fun initRv() {
        rv.layoutManager = LinearLayoutManager(this)
        val dp8 = UiUtils.dp2px(this, 8F)
        val dp16 = UiUtils.dp2px(this, 16F)
        val dp4 = UiUtils.dp2px(this, 4F)
        rv.addItemDecoration(SpaceItemDecoration(dp4, dp8, dp4, dp8).apply {
            setLastBottomPadding(dp16)
        })
        rv.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rv.adapter = adapter
    }

    abstract fun getManagerTitle(): String

    abstract fun getAppEnableStatus(): Map<String, Boolean>

    private fun getData(force: Boolean = false) {
        swipeLayout.isRefreshing = true
        doAsync {
            val map = getAppEnableStatus()
            uiThread {
                swipeLayout.isRefreshing = false
                setData(map, force)
            }
        }
    }

    private fun setData(map: Map<String, Boolean>, force: Boolean) {
        swipeLayout.isRefreshing = false
        adapter.setAppEnableStatus(map, force)
    }
}
