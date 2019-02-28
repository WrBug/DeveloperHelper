package com.wrbug.developerhelper.ipc.processshare

interface DumpDexListProcessData : ProcessData {

    fun setData(list: List<String>)

    fun getData(): ArrayList<String>?
}