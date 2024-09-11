package com.wrbug.developerhelper.util

import android.content.Context
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.toBoolean
import com.wrbug.developerhelper.commonutil.toInt
import com.wrbug.developerhelper.commonutil.toLong
import com.wrbug.developerhelper.model.entity.SharedPreferenceItemInfo
import java.io.File

object OutSharedPreferenceManager {
    fun saveToFile(context: Context, list: Array<SharedPreferenceItemInfo>): File {
        val name = System.currentTimeMillis().toString()
        val sp = context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
        val edit = sp.edit()
        for (sharedPreferenceItemInfo in list) {
            when (sharedPreferenceItemInfo.type.lowercase()) {
                "string" -> {
                    edit.putString(
                        sharedPreferenceItemInfo.key,
                        sharedPreferenceItemInfo.getValidValue()
                    )
                }

                "int" -> {
                    edit.putInt(
                        sharedPreferenceItemInfo.key,
                        sharedPreferenceItemInfo.getValidValue().toInt()
                    )
                }

                "long" -> {
                    edit.putLong(
                        sharedPreferenceItemInfo.key,
                        sharedPreferenceItemInfo.getValidValue().toLong()
                    )
                }

                "boolean" -> {
                    edit.putBoolean(
                        sharedPreferenceItemInfo.key,
                        sharedPreferenceItemInfo.getValidValue().toBoolean()
                    )
                }

                "float" -> {
                    edit.putFloat(
                        sharedPreferenceItemInfo.key,
                        sharedPreferenceItemInfo.getValidValue().toFloat()
                    )
                }
            }
        }
        edit.commit()
        return File("${Constant.dataDir}/${context.packageName}/shared_prefs/$name.xml")
    }
}