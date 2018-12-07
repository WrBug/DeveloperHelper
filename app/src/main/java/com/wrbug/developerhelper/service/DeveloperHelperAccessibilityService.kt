package com.wrbug.developerhelper.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.wrbug.developerhelper.ui.activity.HierarchyActivity
import com.wrbug.developerhelper.model.entry.HierarchyNode
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.util.UiUtils
import java.util.*
import kotlin.collections.ArrayList

class DeveloperHelperAccessibilityService : AccessibilityService() {
    private val receiver = DeveloperHelperAccessibilityReceiver()
    private var nodeId = 0L

    companion object {
        internal var serviceRunning = false
    }

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


    fun readNode(): ArrayList<HierarchyNode> {
        val hierarchyNodes = arrayListOf<HierarchyNode>()
        if (rootInActiveWindow != null) {
            readNodeInfo(hierarchyNodes, rootInActiveWindow, null)
        }
        return hierarchyNodes
    }

    override fun onCreate() {
        super.onCreate()
        serviceRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
    }

    private fun setNodeId(node: HierarchyNode) {
        node.id = nodeId
        nodeId++
    }

    private fun readNodeInfo(
        hierarchyNodes: ArrayList<HierarchyNode>,
        accessibilityNodeInfo: AccessibilityNodeInfo,
        parentNode: HierarchyNode?
    ) {
        if (accessibilityNodeInfo.childCount == 0) {
            return
        }
        for (index in 0 until accessibilityNodeInfo.childCount) {
            val child = accessibilityNodeInfo.getChild(index)
            val screenRect = Rect()
            val parentRect = Rect()
            child.getBoundsInScreen(screenRect)
            child.getBoundsInParent(parentRect)
            screenRect.offset(0, -UiUtils.getStatusHeight())
            val node = HierarchyNode()
            setNodeId(node)
            if (parentNode != null) {
                parentNode.childId.add(node.id)
                node.parentId = parentNode.id
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
            node.screenBounds = screenRect
            node.parentBounds = parentRect
            node.checkable = child.isCheckable
            node.checked = child.isChecked
            node.widget = if (child.className == null) {
                ""
            } else {
                child.className.toString()
            }
            node.clickable = child.isClickable
            node.contentDesc = if (child.contentDescription == null) {
                ""
            } else {
                child.contentDescription.toString()
            }
            node.enabled = child.isEnabled
            node.focusable = child.isFocusable
            node.focused = child.isFocused
            node.longClickable = child.isLongClickable
            node.packagePath = if (child.packageName == null) {
                ""
            } else {
                child.packageName.toString()
            }
            node.password = child.isPassword
            node.scrollable = child.isScrollable
            node.selected = child.isSelected
            readNodeInfo(hierarchyNodes, child, node)
            hierarchyNodes.add(node)
        }
    }


    inner class DeveloperHelperAccessibilityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            val hierarchyNodes = readNode()
            val intent = Intent(context, HierarchyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bundle = Bundle()
            bundle.putParcelableArrayList("node", hierarchyNodes)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }
}