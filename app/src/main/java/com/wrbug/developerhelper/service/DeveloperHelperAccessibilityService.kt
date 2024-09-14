package com.wrbug.developerhelper.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.wrbug.developerhelper.base.BaseApp
import com.wrbug.developerhelper.base.entry.HierarchyNode
import com.wrbug.developerhelper.base.registerReceiverComp
import com.wrbug.developerhelper.commonutil.AppInfoManager
import com.wrbug.developerhelper.commonutil.UiUtils
import com.wrbug.developerhelper.commonutil.entity.ApkInfo
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.constant.ReceiverConstant
import com.wrbug.developerhelper.ui.activity.hierachy.HierarchyActivity


class DeveloperHelperAccessibilityService : AccessibilityService() {
    private val receiver = DeveloperHelperAccessibilityReceiver()
    private var nodeId = 0L
    private var currentAppInfo: ApkInfo? = null
    private var topActivity: TopActivityInfo? = null
    private val activityMap = hashMapOf<String, String>()

    companion object {
        internal var serviceRunning = false
        fun isAccessibilitySettingsOn(): Boolean {
            var accessibilityEnabled = 0
            val service =
                "com.wrbug.developerhelper/" + DeveloperHelperAccessibilityService::class.java.canonicalName
            try {
                accessibilityEnabled = Settings.Secure.getInt(
                    BaseApp.instance.applicationContext.contentResolver,
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (_: Settings.SettingNotFoundException) {
            }

            val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')

            if (accessibilityEnabled == 1) {
                val settingValue = Settings.Secure.getString(
                    BaseApp.instance.applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                if (settingValue != null) {
                    mStringColonSplitter.setString(settingValue)
                    while (mStringColonSplitter.hasNext()) {
                        val accessibilityService = mStringColonSplitter.next()
                        if (accessibilityService.equals(service, ignoreCase = true)) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        val nodeMap: HashMap<Long, HierarchyNode> = hashMapOf()
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            || event.className.isNullOrEmpty()
            || event.className?.startsWith("android.") == true
            || event.packageName.isNullOrEmpty()
        ) {
            return
        }
        runCatching {
            val info = packageManager.getActivityInfo(
                ComponentName(
                    event.packageName.toString(),
                    event.className.toString()
                ), 0
            )
            activityMap[info.packageName] = info.name
        }
    }

    fun readNode(): ArrayList<HierarchyNode> {
        val hierarchyNodes = arrayListOf<HierarchyNode>()
        nodeMap.clear()
        if (rootInActiveWindow != null) {
            rootInActiveWindow.packageName?.run {
                currentAppInfo = AppInfoManager.getAppByPackageName(toString())
            }
            val node = getDecorViewNode(rootInActiveWindow)
            readNodeInfo(hierarchyNodes, node ?: rootInActiveWindow, null)
        }
        return hierarchyNodes
    }


    private fun getDecorViewNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        for (index in 0 until node.childCount) {
            val child = node.getChild(index)
            if (child.viewIdResourceName == "android:id/content") {
                return child
            }
            val decorViewNode = getDecorViewNode(child)
            if (decorViewNode != null) {
                return decorViewNode
            }
        }
        return null
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction(ReceiverConstant.ACTION_HIERARCHY_VIEW)
        registerReceiverComp(receiver, filter)
        sendStatusBroadcast(true)
        serviceRunning = true
        nodeMap.clear()
    }


    override fun onDestroy() {
        super.onDestroy()
        nodeMap.clear()
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
            if (child?.isVisibleToUser != true) {
                continue
            }
            val screenRect = Rect()
            val parentRect = Rect()
            child.getBoundsInScreen(screenRect)
            child.getBoundsInParent(parentRect)
            screenRect.offset(0, -UiUtils.getStatusHeight())
            val node = HierarchyNode()
            setNodeId(node)
            if (parentNode != null) {
                parentNode.childId.add(node)
                nodeMap[node.id] = node
                node.parentId = parentNode.id
            } else {
                hierarchyNodes.add(node)
                nodeMap[node.id] = node
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
            node.packageName = if (child.packageName == null) {
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
        override fun onReceive(context: Context, data: Intent?) {
            val nodesInfo = readNode()
            currentAppInfo?.topActivity =
                activityMap[currentAppInfo?.packageInfo?.packageName].orEmpty()
            HierarchyActivity.start(context, currentAppInfo, nodesInfo)
        }
    }

}