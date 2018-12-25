package com.wrbug.developerhelper.util

import java.lang.reflect.Field
import java.util.HashMap

import android.text.TextUtils
import com.wrbug.developerhelper.model.entity.SharedPreferenceItemInfo
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

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

    /**
     * 将Map对象通过反射机制转换成Bean对象
     *
     * @param map 存放数据的map对象
     * @param clazz 待转换的class
     * @return 转换后的Bean对象
     * @throws Exception 异常
     */
    @Throws(Exception::class)
    fun mapToBean(map: Map<String, Any>?, clazz: Class<*>): Any {
        val obj = clazz.newInstance()
        if (map != null && map.size > 0) {
            for (entry in map.entries) {
                val propertyName = entry.key
                var value: Any? = entry.value
                val setMethodName = ("set"
                        + propertyName.substring(0, 1).toUpperCase()
                        + propertyName.substring(1))
                val field = getClassField(clazz, propertyName)
                val fieldTypeClass = field!!.type
                value = convertValType(value, fieldTypeClass)
                clazz.getMethod(setMethodName, field.type).invoke(obj, value)
            }
        }
        return obj
    }

    /**
     * 将Object类型的值，转换成bean对象属性里对应的类型值
     *
     * @param value Object对象值
     * @param fieldTypeClass 属性的类型
     * @return 转换后的值
     */
    private fun convertValType(value: Any?, fieldTypeClass: Class<*>): Any? {
        var retVal: Any? = null
        if (Long::class.java.name == fieldTypeClass.name || Long::class.javaPrimitiveType!!.name == fieldTypeClass.name) {
            retVal = java.lang.Long.parseLong(value!!.toString())
        } else if (Int::class.java.name == fieldTypeClass.name || Int::class.javaPrimitiveType!!.name == fieldTypeClass.name) {
            retVal = Integer.parseInt(value!!.toString())
        } else if (Float::class.java.name == fieldTypeClass.name || Float::class.javaPrimitiveType!!.name == fieldTypeClass.name) {
            retVal = java.lang.Float.parseFloat(value!!.toString())
        } else if (Double::class.java.name == fieldTypeClass.name || Double::class.javaPrimitiveType!!.name == fieldTypeClass.name) {
            retVal = java.lang.Double.parseDouble(value!!.toString())
        } else {
            retVal = value
        }
        return retVal
    }

    /**
     * 获取指定字段名称查找在class中的对应的Field对象(包括查找父类)
     *
     * @param clazz 指定的class
     * @param fieldName 字段名称
     * @return Field对象
     */
    private fun getClassField(clazz: Class<*>, fieldName: String?): Field? {
        if (Any::class.java.name == clazz.name) {
            return null
        }
        val declaredFields = clazz.declaredFields
        for (field in declaredFields) {
            if (field.name == fieldName) {
                return field
            }
        }

        val superClass = clazz.superclass
        return if (superClass != null) {// 简单的递归一下
            getClassField(superClass, fieldName)
        } else null
    }

    fun toSharedPreference(list: Array<SharedPreferenceItemInfo>): String {
        val sb =
            StringBuilder("<?xml version='1.0' encoding='utf-8' standalone='yes' ?>").append("\n").append("<map>")
        for (sharedPreferenceItemInfo in list) {
            if (!sharedPreferenceItemInfo.isValueValid()) {
                return ""
            }
            sb.append("\n").append("<").append(sharedPreferenceItemInfo.type).append(" ").append("name=\"")
                .append(sharedPreferenceItemInfo.key).append("\"")
            if (sharedPreferenceItemInfo.type != "string") {
                sb.append(" ").append("value=\"${sharedPreferenceItemInfo.newValue}\"").append(" />")
            } else {
//                .replace("\"", "&quot;")
                sb.append(">").append(sharedPreferenceItemInfo.newValue)
                    .append("</string>")
            }
        }
        sb.append("</map>")
        return sb.toString()

    }
}