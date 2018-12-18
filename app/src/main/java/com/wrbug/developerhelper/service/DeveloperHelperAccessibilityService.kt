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
import com.wrbug.developerhelper.ui.activity.hierachy.HierarchyActivity
import com.wrbug.developerhelper.model.entry.HierarchyNode
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.model.entry.ApkInfo
import com.wrbug.developerhelper.model.entry.TopActivityInfo
import com.wrbug.developerhelper.shell.Callback
import com.wrbug.developerhelper.shell.ShellManager
import com.wrbug.developerhelper.util.AppInfoManager
import com.wrbug.developerhelper.util.UiUtils
import kotlin.collections.ArrayList

class DeveloperHelperAccessibilityService : AccessibilityService() {
    private val receiver = DeveloperHelperAccessibilityReceiver()
    private var nodeId = 0L
    private var currentAppInfo: ApkInfo? = null
    private var topActivity: TopActivityInfo? = null

    companion object {
        internal var serviceRunning = false
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onServiceConnected() {
        super.onServiceConnected()

    }

    fun readNode(): ArrayList<HierarchyNode> {
        val hierarchyNodes = arrayListOf<HierarchyNode>()
        if (rootInActiveWindow != null) {
            rootInActiveWindow.packageName?.run {
                currentAppInfo = AppInfoManager.getAppByPackageName(toString())
            }
            readNodeInfo(hierarchyNodes, rootInActiveWindow, null)
        }
        return hierarchyNodes
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction(ReceiverConstant.ACTION_HIERARCHY_VIEW)
        registerReceiver(receiver, filter)
        sendStatusBroadcast(true)
        serviceRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceRunning = false
        unregisterReceiver(receiver)
        sendStatusBroadcast(false)
    }

    private fun setNodeId(node: HierarchyNode) {
        node.id = nodeId
        nodeId++
    }

    private fun sendStatusBroadcast(running: Boolean) {
        val intent = Intent(ReceiverConstant.ACTION_ACCESSIBILITY_SERVICE_STATUS_CHANGED)
        intent.putExtra("status", running)
        sendBroadcast(intent)
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
                parentNode.childId.add(node)
                node.parentId = parentNode.id
            } else {
                hierarchyNodes.add(node)
            }
            child.viewIdResourceName?.let {
                node.resourceId = it
                if (it.contains(":id")) {
                    node.idHex = topActivity?.viewIdHex?.get(it.substring(it.indexOf("id")))
                }
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
        }
    }

    inner class DeveloperHelperAccessibilityReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            ShellManager.getTopActivity(object : Callback<TopActivityInfo> {
                override fun onFailed() {

                }

                override fun onSuccess(data: TopActivityInfo) {
                    topActivity = data
                    val hierarchyNodes = readNode()
                    val intent = Intent(context, HierarchyActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val bundle = Bundle()
                    bundle.putParcelable("apkInfo", currentAppInfo)
                    bundle.putParcelableArrayList("node", hierarchyNodes)
                    bundle.putParcelable("topActivity", topActivity)
                    intent.putExtras(bundle)
                    startActivity(intent)
                }
            })

        }
    }
}