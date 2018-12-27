package com.wrbug.developerhelper.ui.activity.hierachy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.model.entity.ApkInfo
import com.wrbug.developerhelper.model.entity.HierarchyNode
import com.wrbug.developerhelper.model.entity.TopActivityInfo
import com.wrbug.developerhelper.ui.widget.hierarchyView.HierarchyView
import kotlinx.android.synthetic.main.activity_hierarchy.*

class HierarchyActivity : BaseActivity(), AppInfoDialogEventListener {


    private var apkInfo: ApkInfo? = null
    private var nodeList: List<HierarchyNode>? = null
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hierarchy)
        apkInfo = intent.getParcelableExtra("apkInfo")
        nodeList = intent.getParcelableArrayListExtra("node")
        topActivity = intent.getParcelableExtra("topActivity")
        showAppInfoDialog()
        setFloatButtonVisible(false)
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
            }

        })
    }

    override fun onDestroy() {
        setFloatButtonVisible(true)
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


    private fun setFloatButtonVisible(visible: Boolean) {
        val intent = Intent(ReceiverConstant.ACTION_SET_FLOAT_BUTTON_VISIBLE)
        intent.putExtra("visible", visible)
        sendBroadcast(intent)
    }
}
