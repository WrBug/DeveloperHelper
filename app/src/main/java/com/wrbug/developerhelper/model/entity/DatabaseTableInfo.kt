package com.wrbug.developerhelper.model.entity

class DatabaseTableInfo {
    var name = ""
    var keys = arrayOf<String>()
    var count = 0
    var rows: List<Map<String, String?>> = arrayListOf()
        set(value) {
            field = value
            count = value.size
        }
}