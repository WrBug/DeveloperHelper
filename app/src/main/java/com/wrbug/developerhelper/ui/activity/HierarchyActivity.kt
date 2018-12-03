package com.wrbug.developerhelper.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.wrbug.developerhelper.HierarchyNode
import com.wrbug.developerhelper.R
import com.wrbug.developerhelper.constant.ReceiverConstant
import kotlinx.android.synthetic.main.activity_hierarchy.*
import java.util.HashMap

class HierarchyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hierarchy)
        val list = intent.getSerializableExtra("node") as? HashMap<Long, HierarchyNode>
        setFloatViewVisible(false)
        hierarchyView.setHierarchyNodes(list)
    }


    override fun onDestroy() {
        setFloatViewVisible(true)
        super.onDestroy()
    }


    private fun setFloatViewVisible(visible: Boolean) {
        val intent = Intent(ReceiverConstant.ACTION_SET_FLOAT_VIEW_VISIBLE)
        intent.putExtra("visible", visible)
        sendBroadcast(intent)
    }
}
