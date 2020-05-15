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
    fun isXposedOpenAsync(): Observable<Boolean> {
        if (processData == null) {
            return Observable.just(
                false
            )
        }
        return processData!!.isXposedOpen()
            .map { it?.toBoolean() ?: false }
            .onErrorResumeNext {
                it.print()
                Observable.just(false)
            }
    }

    fun isXposedOpen(): Boolean {
        return isXposedOpenAsync().blockingFirst()
    }

    fun setXposedOpen(open: Boolean) {
        processData?.setXposedOpen(open)
    }

    companion object {
        val instance = GlobalConfigProcessDataManager()
    }
}