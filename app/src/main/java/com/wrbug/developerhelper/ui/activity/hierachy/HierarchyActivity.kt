package com.wrbug.developerhelper.ui.activity.hierachy

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.basecommon.BaseActivity
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.model.entry.ApkInfo
import com.wrbug.developerhelper.model.entry.HierarchyNode
import com.wrbug.developerhelper.ui.widget.hierarchyView.HierarchyView
import kotlinx.android.synthetic.main.activity_hierarchy.*

class HierarchyActivity : BaseActivity(), AppInfoDialogEventListener {


    private var apkInfo: ApkInfo? = null
    private var nodeList: List<HierarchyNode>? = null
    private var showHierachyView = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hierarchy)
        apkInfo = intent.getParcelableExtra("apkInfo")
        nodeList = intent.getParcelableArrayListExtra("node")
        setFloatButtonVisible(false)
        showAppInfoDialog()
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
