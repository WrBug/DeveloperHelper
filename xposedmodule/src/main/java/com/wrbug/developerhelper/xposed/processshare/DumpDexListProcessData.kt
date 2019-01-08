package com.wrbug.developerhelper.xposed.processshare

interface DumpDexListProcessData : ProcessData {

    fun setData(list: List<String>)

    fun getData(): List<String>?
}