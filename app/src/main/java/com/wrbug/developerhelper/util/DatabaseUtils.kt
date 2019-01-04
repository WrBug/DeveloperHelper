package com.wrbug.developerhelper.util

import android.database.sqlite.SQLiteDatabase
import com.wrbug.developerhelper.model.entity.DatabaseTableInfo
import java.util.*

object DatabaseUtils {
    fun getDatabase(dbFile: String): Map<String, DatabaseTableInfo> {
        val map = TreeMap<String, DatabaseTableInfo>()
        val db = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.OPEN_READWRITE)
        db?.run {
            val cursor = rawQuery("select name from sqlite_master where type='table' order by name", null)
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                if (name == "android_metadata") {
                    continue
                }
                val rawQuery = rawQuery("select * from $name", null)
                val count = rawQuery.columnCount
                val info = DatabaseTableInfo()
                info.name = name
                info.keys = rawQuery.columnNames
                val list = ArrayList<Map<String, String?>>()
                while (rawQuery.moveToNext()) {
                    val dataMap = hashMapOf<String, String?>()
                    for (index in 0 until count) {
                        dataMap[rawQuery.getColumnName(index)] = rawQuery.getString(index)
                    }
                    list.add(dataMap)
                }
                rawQuery.close()
                info.rows = list
                map[name] = info
            }
            cursor.close()
            close()
        }
        return map
    }
}