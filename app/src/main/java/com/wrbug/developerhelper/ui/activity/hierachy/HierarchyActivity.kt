package com.wrbug.developerhelper.ui.activity.hierachy

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import com.google.gson.reflect.TypeToken
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.model.entity.HierarchyNode
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.ui.widget.hierarchyView.HierarchyView
import com.wrbug.developerhelper.commonutil.JsonHelper
import com.wrbug.developerhelper.constant.ReceiverConstant.ACTION_FINISH_HIERACHY_Activity
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.widget.layoutinfoview.LayoutInfoView
import kotlinx.android.synthetic.main.activity_hierarchy.*
import java.lang.ref.WeakReference

class HierarchyActivity : BaseActivity(), AppInfoDialogEventListener {


    private var apkInfo: ApkInfo? = null
    private var nodeList: List<HierarchyNode>? = null
    private var nodeMap: HashMap<Long, HierarchyNode>? = null
    private var showHierachyView = false
    private var topActivity: TopActivityInfo? = null

    companion object {
        fun start(
            context: Context?,
            apkInfo: ApkInfo?,
            node: ArrayList<HierarchyNode>?,
            topActivity: TopActivityInfo?
        ) {
            val intent = Intent(context, HierarchyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            bundle.putParcelable("apkInfo", apkInfo)
            bundle.putParcelableArrayList("node", node)
            bundle.putParcelable("topActivity", topActivity)
            intent.putExtras(bundle)
            context?.startActivity(intent)
        }

        private var receiver = object : BroadcastReceiver() {
            private var reference: WeakReference<Activity>? = null
            fun setActivity(activity: Activity) {
                reference = WeakReference(activity)
            }

            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ReceiverConstant.ACTION_FINISH_HIERACHY_Activity -> {
                        reference?.get()?.finish()
                    }

                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hierarchy)
        intent?.run {
            apkInfo = getParcelableExtra("apkInfo")
            nodeList = getParcelableArrayListExtra("node")
            topActivity = getParcelableExtra("topActivity")
            val json = getStringExtra("nodeMap")
            nodeMap = JsonHelper.fromJson(json, object : TypeToken<HashMap<Long, HierarchyNode>>() {}.type)
        }
        val filter = IntentFilter(ACTION_FINISH_HIERACHY_Activity)
        receiver.setActivity(this)
        registerReceiver(receiver, filter)
        showAppInfoDialog()
        FloatWindowService.setFloatButtonVisible(this, false)
    }

    private fun showAppInfoDialog() {
        val dialog = AppInfoDialog()
        val bundle = Bundle()
        bundle.putParcelable("apkInfo", apkInfo)
        bundle.putParcelable("topActivity", topActivity)
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "")
    }

    override fun showHierachyView() {
        showHierachyView = true
        hierarchyView.setHierarchyNodes(nodeList)
        hierarchyView.setOnHierarchyNodeClickListener(object : HierarchyView.OnHierarchyNodeClickListener {
            override fun onClick(node: HierarchyNode, parentNode: HierarchyNode?) {
                hierarchyDetailView.visibility = View.VISIBLE
                hierarchyDetailView.setNode(node, parentNode)
                LayoutInfoView(context,nodeList, node).show()
            }

            override fun onSelectedNodeChanged(node: HierarchyNode, parentNode: HierarchyNode?) {
                hierarchyDetailView.visibility = View.VISIBLE
                hierarchyDetailView.setNode(node, parentNode)
            }
        })
    }

    override fun onDestroy() {
        FloatWindowService.setFloatButtonVisible(this, true)
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun close() {
        if (!showHierachyView) {
            finish()
        }
    }

    override fun onBackPressed() {
        if (hierarchyDetailView.visibility == View.VISIBLE) {
            hierarchyDetailView.visibility = View.GONE
            return
        }
        super.onBackPressed()
    }


}
