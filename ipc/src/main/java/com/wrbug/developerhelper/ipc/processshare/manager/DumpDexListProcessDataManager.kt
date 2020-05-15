package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData


/**
 *
 *  class: DumpDexListProcessDataManager.kt
 *  author: wrbug
 *  date: 2020-05-15
 *  description：
 *
 */
class DumpDexListProcessDataManager private constructor() :
    ProcessDataManager<DumpDexListProcessData>(),
    DumpDexListProcessData {
    override fun setData(list: List<String>) {
        processData?.setData(list)
    }

    override fun getData() = processData?.getData()

    companion object {
         val instance = DumpDexListProcessDataManager()
    }
}