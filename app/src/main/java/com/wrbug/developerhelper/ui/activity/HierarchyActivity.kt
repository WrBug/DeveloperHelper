package com.wrbug.developerhelper.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.wrbug.developerhelper.HierarchyNode
import com.wrbug.developerhelper.R
import kotlinx.android.synthetic.main.activity_hierarchy.*
import java.util.HashMap

class HierarchyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hierarchy)
        val list = intent.getSerializableExtra("node") as? HashMap<Long, HierarchyNode>
        hierarchyView.setHierarchyNodes(list)
    }
}
