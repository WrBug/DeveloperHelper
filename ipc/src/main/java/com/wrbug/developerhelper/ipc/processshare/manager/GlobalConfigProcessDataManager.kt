package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.commonutil.print
import com.wrbug.developerhelper.ipc.processshare.GlobalConfigProcessData
import io.reactivex.rxjava3.core.Observable


/**
 *
 *  class: GlobalConfigProcessDataManager.kt
 *  author: wrbug
 *  date: 2020-05-14
 *  description：
 *
 */
class GlobalConfigProcessDataManager private constructor() :
    ProcessDataManager<GlobalConfigProcessData>() {
    fun isXposedOpen(): Boolean {
        return processData.isXposedOpen().blockingFirst()
    }

    fun setXposedOpen(open: Boolean) {
        processData.setXposedOpen(open)
    }

    companion object {
        val instance = GlobalConfigProcessDataManager()
    }
}