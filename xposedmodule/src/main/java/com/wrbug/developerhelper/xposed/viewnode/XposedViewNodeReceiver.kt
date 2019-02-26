package com.wrbug.developerhelper.xposed.viewnode

import android.app.Activity
import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import com.wrbug.developerhelper.basecommon.entry.HierarchyNode
import com.wrbug.developerhelper.commonutil.entity.FragmentInfo
import com.wrbug.developerhelper.commonutil.entity.TopActivityInfo
import com.wrbug.developerhelper.commonutil.toInt
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.Exception
import java.lang.ref.WeakReference
import java.lang.reflect.Method

class XposedViewNodeReceiver(activity: Activity) : BroadcastReceiver() {
    companion object {
        private var currentId = 0L
    }

    private val reference = WeakReference(activity)
    override fun onReceive(context: Context?, intent: Intent?) {
        reference.get()?.apply {
            Toast.makeText(this, "正在获取应用信息", Toast.LENGTH_SHORT).show()
            val decorView = window.decorView
            val nodes = ArrayList<HierarchyNode>()
            getViewNode(nodes, decorView)
            val topActivityInfo = TopActivityInfo()
            topActivityInfo.activity = javaClass.name
//            fragmentManager.findFragmentById()
            val fragments = ArrayList<FragmentInfo>()
            getFragment(this, fragments)
            topActivityInfo.fragments = fragments.toTypedArray()
        }

    }

    private fun getFragment(
        activity: Activity,
        fragments: ArrayList<FragmentInfo>
    ) {
        var method: Method? = null
        try {
            method = activity.javaClass.getMethod("getSupportFragmentManager") ?: return
        } catch (e: Exception) {
            XposedBridge.log(e)
        }
        if (method == null) {
            try {
                method = activity.javaClass.getMethod("getFragmentManager") ?: return
            } catch (e: Exception) {
                XposedBridge.log(e)
            }
        }
        if (method == null) {
            return
        }
        val fm = XposedHelpers.callMethod(activity, method.name)
        val list = XposedHelpers.getObjectField(fm, "mBackStackIndices") as List<*>?
        if (list.isNullOrEmpty()) {
            return
        }
        list.last()?.let {
            val mOps = XposedHelpers.getObjectField(this, "mOps") as List<*>
            if (mOps.isNullOrEmpty().not()) {
                for (index in mOps.size - 1 downTo 0) {
                    mOps[index]?.let {
                        val fragment = XposedHelpers.getObjectField(it, "fragment") as Fragment
                        val info = FragmentInfo()
                        info.added = XposedHelpers.callMethod(fragment, "isAdded") as Boolean
                        val id = XposedHelpers.callMethod(fragment, "getId") as Int
                        if (id != View.NO_ID) {
                            info.containerId = activity.resources.getResourceName(id)
                        }
                        info.detached = XposedHelpers.callMethod(fragment, "isDetached") as Boolean
                        info.hidden = XposedHelpers.callMethod(fragment, "isHidden") as Boolean
                        info.name = it.javaClass.name
                        info.removing = XposedHelpers.callMethod(fragment, "isRemoving") as Boolean
                        info.index = index
                        fragments.add(info)
                    }

                }
            }
        }

    }

    private fun getViewNode(nodes: ArrayList<HierarchyNode>, view: View, parent: HierarchyNode? = null) {
        val node = HierarchyNode()
        node.id = currentId++
        node.checkable = view is CompoundButton && view.isClickable
        node.checked = view is CompoundButton && view.isChecked
        parent?.let {
            node.parentId = it.id
            it.childId.add(node)
        }
        node.clickable = view.isClickable
        node.enabled = view.isEnabled
        node.focusable = view.isFocusable
        node.focused = view.isFocused
        if (view.id == View.NO_ID) {
            node.idHex = "NO_ID"
        } else {
            node.idHex = "0x" + Integer.toHexString(view.id)
            try {
                node.resourceId = view.resources.getResourceName(view.id)
            } catch (e: Exception) {

            }
        }
        node.longClickable = view.isLongClickable
        node.packageName = view.context.packageName
        node.parentId = parent?.id ?: 0
        node.password = view is TextView && (view.inputType and EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD == 1 ||
                view.inputType and EditorInfo.TYPE_TEXT_VARIATION_PASSWORD == 1 ||
                view.inputType and EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == 1 ||
                view.inputType and EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD == 1)
        node.scrollable = view is ViewGroup && (view.canScrollHorizontally(1) || view.canScrollVertically(1))
        node.selected = view.isSelected
        node.text = if (view is TextView) view.text.toString() else ""
        node.widget = view.javaClass.name
        node.parentBounds = parent?.screenBounds
        node.screenBounds = Rect(
            view.left + node.parentBounds?.left.toInt(),
            view.left + node.parentBounds?.left.toInt(),
            view.left + node.parentBounds?.left.toInt(),
            view.left + node.parentBounds?.left.toInt()
        )
        if (parent == null) {
            nodes.add(node)
        }
        if (view is ViewGroup) {
            val childCount = view.childCount
            for (i in 0 until childCount) {
                view.getChildAt(i)?.apply {
                    getViewNode(nodes, this, node)
                }
            }
        }
    }
}