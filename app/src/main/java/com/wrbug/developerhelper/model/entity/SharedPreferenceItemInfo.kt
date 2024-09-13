package com.wrbug.developerhelper.model.entity

import com.wrbug.developerhelper.commonutil.*

class SharedPreferenceItemInfo {
    var key: String = ""
    var value = ""
    var type = "string"
    var newValue = ""


    fun isValueValid(): Boolean {
        when (type.lowercase()) {
            "string" -> {
                return JsonHelper.fromJson(value) == null || JsonHelper.fromJson(newValue) != null
            }
            "int" -> {
                return newValue.isInt()
            }
            "long" -> {
                return newValue.isNumber()

            }

            "boolean" -> {
                return newValue.isBoolean()

            }
            "float" -> {
                return newValue.isDecimal()
            }
            else -> return true
        }

    }


    fun getValidValue(): String {
        if (isValueValid()) {
            return newValue
        }
        return value
    }
}