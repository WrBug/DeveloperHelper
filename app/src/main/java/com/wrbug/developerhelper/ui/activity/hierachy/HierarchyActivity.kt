package com.wrbug.developerhelper.ui.activity.hierachy

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import com.wrbug.developerhelper.base.BaseActivity
import com.wrbug.developerhelper.base.entry.HierarchyNode
import com.wrbug.developerhelper.base.registerReceiverComp
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.constant.ReceiverConstant.ACTION_FINISH_HIERACHY_Activity
import com.wrbug.developerhelper.databinding.ActivityHierarchyBinding
import com.wrbug.developerhelper.service.FloatWindowService
import com.wrbug.developerhelper.ui.widget.hierarchyView.HierarchyView
import com.wrbug.developerhelper.ui.widget.layoutinfoview.LayoutInfoDialog
import java.lang.ref.WeakReference

class HierarchyActivity : BaseActivity(), AppInfoDialogEventListener {

    private val apkInfo: ApkInfo? by lazy {
        intent?.getParcelableExtra("apkInfo")
    }
    private val nodeList: ArrayList<HierarchyNode>? by lazy {
        intent?.getParcelableArrayListExtra("node")
    }
    private var showHierachyView = false
    private lateinit var binding: ActivityHierarchyBinding

    companion object {

        fun start(
            context: Context?, apkInfo: ApkInfo?, node: ArrayList<HierarchyNode>
        ) {
            val intent = Intent(context, HierarchyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            bundle.putParcelable("apkInfo", apkInfo)
            bundle.putParcelableArrayList("node", node)
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
                    ACTION_FINISH_HIERACHY_Activity -> {
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
        checkNodeList()
        val filter = IntentFilter(ACTION_FINISH_HIERACHY_Activity)
        receiver.setActivity(this)
        registerReceiverComp(receiver, filter)
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
        dialog.arguments = bundle
        dialog.show(supportFragmentManager, "")
    }

    override fun showHierachyView() {
        showHierachyView = true
        binding.hierarchyView.setHierarchyNodes(nodeList)
        binding.hierarchyView.setOnHierarchyNodeClickListener(object :
            HierarchyView.OnHierarchyNodeClickListener {
            override fun onClick(node: HierarchyNode, parentNode: HierarchyNode?) {
                binding.hierarchyDetailView.visibility = View.VISIBLE
                binding.hierarchyDetailView.setNode(node, parentNode)
                LayoutInfoDialog.show(supportFragmentManager, nodeList, node) { node, parentNode ->
                    binding.hierarchyDetailView.setNode(node, parentNode)
                }
            }

            override fun onSelectedNodeChanged(node: HierarchyNode, parentNode: HierarchyNode?) {
                binding.hierarchyDetailView.visibility = View.VISIBLE
                binding.hierarchyDetailView.setNode(node, parentNode)
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
