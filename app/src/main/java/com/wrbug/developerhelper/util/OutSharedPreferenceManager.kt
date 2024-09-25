package com.wrbug.developerhelper.util

import android.annotation.SuppressLint
import android.content.Context
import com.wrbug.developerhelper.commonutil.Constant
import com.wrbug.developerhelper.commonutil.shell.ShellManager
import com.wrbug.developerhelper.commonutil.toBoolean
import com.wrbug.developerhelper.commonutil.toInt
import com.wrbug.developerhelper.commonutil.toLong
import com.wrbug.developerhelper.model.entity.SharedPreferenceItemInfo
import io.reactivex.rxjava3.core.Single
import java.io.File

class OutSharedPreference(private val context: Context, private val filePath: String) {
    private val fileName by lazy {
        System.currentTimeMillis().toString()
    }
    private val tmpSpFile by lazy {
        File("${Constant.getDataDir(context.packageName)}/shared_prefs/${fileName}.xml")
    }
    private val tmpFile by lazy {
        File(context.cacheDir, "$fileName.xml")
    }

    fun parse(): Single<Array<SharedPreferenceItemInfo>> {
        return Single.just(filePath).map {
            if (!ShellManager.catFile(it, tmpFile.absolutePath, "777")) {
                return@map emptyArray()
            }
            val xml = tmpFile.readText()
            XmlUtil.parseSharedPreference(xml)
        }
    }

    @SuppressLint("ApplySharedPref")
    fun saveToFile(context: Context, list: Array<SharedPreferenceItemInfo>): Single<Boolean> {
        return Single.just(list).map {
            val sp = context.applicationContext.getSharedPreferences(fileName, Context.MODE_PRIVATE)
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

                    "set" -> {
                        edit.putStringSet(
                            sharedPreferenceItemInfo.key,
                            sharedPreferenceItemInfo.getValidValue().split(",").toSet()
                        )
                    }
                }
            }
            edit.commit()
            ShellManager.catFile(tmpSpFile.absolutePath, filePath, "666")
        }.onErrorReturn { false }
    }


    fun deleteTmpFile() {
        tmpSpFile.delete()
        tmpFile.delete()
    }
}