package com.wrbug.developerhelper.ui.activity.hierachy

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import com.google.gson.reflect.TypeToken
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.basecommon.entry.HierarchyNode
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.ui.widget.hierarchyView.HierarchyView
import com.wrbug.developerhelper.commonutil.JsonHelper
import com.wrbug.developerhelper.constant.ReceiverConstant.ACTION_FINISH_HIERACHY_Activity
import com.wrbug.developerhelper.databinding.ActivityHierarchyBinding
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.widget.layoutinfoview.LayoutInfoView
import com.wrbug.developerhelper.ui.widget.layoutinfoview.OnNodeChangedListener
import java.lang.ref.WeakReference

class HierarchyActivity: BaseActivity(), AppInfoDialogEventListener, OnNodeChangedListener {

    private var apkInfo: ApkInfo? = null
    private var nodeList: ArrayList<HierarchyNode>? = null
    private var nodeMap: HashMap<Long, HierarchyNode>? = null
    private var showHierachyView = false
    private var topActivity: TopActivityInfo? = null
    private lateinit var binding: ActivityHierarchyBinding

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

        private var receiver = object: BroadcastReceiver() {
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
        binding = ActivityHierarchyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent?.run {
            apkInfo = getParcelableExtra("apkInfo")
            nodeList = getParcelableArrayListExtra("node")
            checkNodeList()
            topActivity = getParcelableExtra("topActivity")
            val json = getStringExtra("nodeMap")
            nodeMap = JsonHelper.fromJson(
                json,
                object: TypeToken<HashMap<Long, HierarchyNode>>() {}.type
            )
        }
        val filter = IntentFilter(ACTION_FINISH_HIERACHY_Activity)
        receiver.setActivity(this)
        registerReceiver(receiver, filter)
        showAppInfoDialog()
        FloatWindowService.setFloatButtonVisible(this, false)
    }

    private fun checkNodeList() {
        nodeList ?: return
        if ((nodeList?.size ?: 0) <= 1) {
            return
        }
        var hierarchyNode: HierarchyNode? = null
        nodeList?.forEach {
            if (hierarchyNode == null) {
                hierarchyNode = it
            } else if (hierarchyNode?.screenBounds?.let { it1 -> it.screenBounds?.contains(it1) } == true) {
                hierarchyNode = it
            }
        }
        nodeList?.clear()
        hierarchyNode?.let {
            nodeList?.add(it)
        }
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
        binding.hierarchyView.setHierarchyNodes(nodeList)
        binding.hierarchyView.setOnHierarchyNodeClickListener(object:
            HierarchyView.OnHierarchyNodeClickListener {
            override fun onClick(node: HierarchyNode, parentNode: HierarchyNode?) {
                binding.hierarchyDetailView.visibility = View.VISIBLE
                binding.hierarchyDetailView.setNode(node, parentNode)
                val layoutInfoView = LayoutInfoView(context, nodeList, node)
                layoutInfoView.setOnNodeChangedListener(this@HierarchyActivity)
                layoutInfoView.show()
            }

            override fun onSelectedNodeChanged(node: HierarchyNode, parentNode: HierarchyNode?) {
                binding.hierarchyDetailView.visibility = View.VISIBLE
                binding.hierarchyDetailView.setNode(node, parentNode)
            }
        })
    }

    override fun onChanged(node: HierarchyNode, parentNode: HierarchyNode?) {
        binding.hierarchyDetailView.setNode(node, parentNode)
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
        if (binding.hierarchyDetailView.visibility == View.VISIBLE) {
            binding.hierarchyDetailView.visibility = View.GONE
            return
        }
        if (showHierachyView) {
            showHierachyView = false
            binding.hierarchyView.visibility = View.GONE
            showAppInfoDialog()
            return
        }
        super.onBackPressed()
    }

}
