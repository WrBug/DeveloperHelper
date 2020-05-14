package com.wrbug.developerhelper.ipc.processshare

import androidx.annotation.Keep

@Keep
interface DumpDexListProcessData : ProcessData {

    fun setData(list: List<String>)

    fun getData(): ArrayList<String>?
}