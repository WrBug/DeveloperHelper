package com.wrbug.developerhelper.util

import com.wrbug.developerhelper.model.entity.SharedPreferenceItemInfo
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.tree.DefaultElement

/**
 * xml相关的工具类
 *
 * @author yang.y
 */
object XmlUtil {

    fun parseSharedPreference(xmlStr: String): Array<SharedPreferenceItemInfo> {
        if (xmlStr.isEmpty()) {
            return arrayOf()
        }
        val doc = DocumentHelper.parseText(xmlStr)
        val root = doc.rootElement
        val children = root.elements()
        val list = ArrayList<SharedPreferenceItemInfo>()
        if (children != null && children.size > 0) {
            for (item in children) {
                val child = item as Element
                val info = SharedPreferenceItemInfo()
                val type = child.name
                info.key = child.attributeValue("name")
                if (type == "string") {
                    info.value = child.text
                    info.newValue = child.text
                } else if (type == "set") {
                    info.value = child.content().filterIsInstance(DefaultElement::class.java)
                        .joinToString(",") { it.text }
                    info.newValue = child.content().filterIsInstance(DefaultElement::class.java)
                        .joinToString(",") { it.text }
                } else {
                    info.value = child.attributeValue("value")
                    info.newValue = child.attributeValue("value")
                }
                info.type = type
                list.add(info)
            }
        }
        return list.toTypedArray()
    }

}