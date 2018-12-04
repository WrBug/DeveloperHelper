package com.wrbug.developerhelper.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.wrbug.developerhelper.HierarchyNode
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.ui.widget.hierarchyView.HierarchyView
import kotlinx.android.synthetic.main.activity_hierarchy.*
import java.util.HashMap

class HierarchyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hierarchy)
        val list = intent.getSerializableExtra("node") as? Map<Long, HierarchyNode>
        setFloatViewVisible(false)
        hierarchyView.setHierarchyNodes(list)
        hierarchyView.setOnHierarchyNodeClickListener(object : HierarchyView.OnHierarchyNodeClickListener {
            override fun onClick(node: HierarchyNode) {
                hierarchyDetailView.visibility = View.VISIBLE
                hierarchyDetailView.hierarchyNode = node
            }

        })
    }


    override fun onDestroy() {
        setFloatViewVisible(true)
        hideLayoutInfoView()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (hierarchyDetailView.visibility == View.VISIBLE) {
            hierarchyDetailView.visibility = View.GONE
            return
        }
        super.onBackPressed()
    }

    private fun setFloatViewVisible(visible: Boolean) {
        val intent = Intent(ReceiverConstant.ACTION_SET_FLOAT_VIEW_VISIBLE)
        intent.putExtra("visible", visible)
        sendBroadcast(intent)
    }

    private fun hideLayoutInfoView() {
        val intent = Intent(ReceiverConstant.ACTION_HIDE_LAYOUT_INFO_VIEW)
        sendBroadcast(intent)
    }
}
