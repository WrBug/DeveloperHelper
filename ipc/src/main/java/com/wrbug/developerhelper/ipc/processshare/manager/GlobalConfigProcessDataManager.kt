package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.ipc.processshare.GlobalConfigProcessData


/**
 *
 *  class: GlobalConfigProcessDataManager.kt
 *  author: wrbug
 *  date: 2020-05-14
 *  description：
 *
 */
class GlobalConfigProcessDataManager private constructor() :
    ProcessDataManager<GlobalConfigProcessData>(), GlobalConfigProcessData {
    private var isOpen: Boolean? = null
    override fun isXposedOpen(): Boolean {
        if (isOpen != null) {
            return isOpen as Boolean
        }
        isOpen = processData?.isXposedOpen()
        return isOpen ?: false
    }

    override fun setXposedOpen(open: Boolean) {
        isOpen = open
        processData?.setXposedOpen(open)
    }

    companion object {
        val instance = GlobalConfigProcessDataManager()
    }
}