package com.wrbug.developerhelper.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.wrbug.developerhelper.ui.activity.HierarchyActivity
import com.wrbug.developerhelper.HierarchyNode
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.util.JsonHelper
import java.util.HashMap

class DeveloperHelperAccessibilityService : AccessibilityService() {
    private val receiver = DeveloperHelperAccessibilityReceiver()
    var hierarchyNodes = HashMap<Long, HierarchyNode>()
    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter()
        filter.addAction(ReceiverConstant.ACTION_HIERARCHY_VIEW)
        registerReceiver(receiver, filter)
    }


    fun readNode(): String {
        if (rootInActiveWindow != null) {
            hierarchyNodes.clear()
            readNodeInfo(rootInActiveWindow, 0L)
            return JsonHelper.toJson(hierarchyNodes)
        }
        return ""
    }

    private fun readNodeInfo(accessibilityNodeInfo: AccessibilityNodeInfo, parentId: Long) {
        Log.i("aaa", "count=" + accessibilityNodeInfo.childCount)
        if (accessibilityNodeInfo.childCount == 0) {
            return
        }
        for (index in 0 until accessibilityNodeInfo.childCount) {
            val child = accessibilityNodeInfo.getChild(index)
            val rect = Rect()
            child.getBoundsInScreen(rect)
            rect.offset(0, -60)
            val node = HierarchyNode()
            if (parentId != 0L) {
                node.id = node.id + parentId + index
                hierarchyNodes[node.id] = node
                val hierarchyNode = hierarchyNodes[parentId]
                hierarchyNode?.childId?.add(node.id)
                node.parentId = hierarchyNode?.id!!
            } else {
                node.id = node.id + index
                hierarchyNodes[node.id] = node
            }
            node.resourceId = if (child.viewIdResourceName == null) {
                ""
            } else {
                child.viewIdResourceName
            }
            var text = ""
            if (child.text != null) {
                text = child.text.toString()
            }
            node.text = text
            node.bounds = rect
            node.checkable = child.isCheckable
            node.checked = child.isChecked
            node.classPath = if (child.className == null) {
                ""
            } else {
                child.className.toString()
            }
            node.clickable = child.isClickable
            node.contentdesc = if (child.contentDescription == null) {
                ""
            } else {
                child.contentDescription.toString()
            }
            node.enabled = child.isEnabled
            node.focusable = child.isFocusable
            node.focused = child.isFocused
            node.longclickable = child.isLongClickable
            node.packagePath = if (child.packageName == null) {
                ""
            } else {
                child.packageName.toString()
            }
            node.password = child.isPassword
            node.scrollable = child.isScrollable
            node.selected = child.isSelected
            readNodeInfo(child, node.id)
        }
    }


    inner class DeveloperHelperAccessibilityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            Log.i("aaaa", data?.action)
            Log.i("aaaa", readNode())
            val intent = Intent(context, HierarchyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            bundle.putSerializable("node", hierarchyNodes)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}