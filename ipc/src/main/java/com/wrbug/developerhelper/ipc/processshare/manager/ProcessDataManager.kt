package com.wrbug.developerhelper.ipc.processshare.manager

import com.wrbug.developerhelper.ipc.processshare.ProcessData
import com.wrbug.developerhelper.ipc.processshare.ProcessDataCreator
import java.lang.reflect.ParameterizedType


/**
 *
 *  class: ProcessDataCreator.kt
 *  author: wrbug
 *  date: 2020-05-14
 *  description：
 *
 */
open class ProcessDataManager<T : ProcessData> {


    protected val processData: T? by lazy {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        ProcessDataCreator.get(type as Class<T>)
    }
}