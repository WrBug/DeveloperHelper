package com.wrbug.developerhelper.ipc.processshare.data

import com.wrbug.developerhelper.ipc.processshare.AppXposedProcessData
import com.wrbug.developerhelper.ipc.processshare.DataFinderListProcessData
import com.wrbug.developerhelper.ipc.processshare.DumpDexListProcessData
import com.wrbug.developerhelper.ipc.processshare.GlobalConfigProcessData
import java.io.File

object IpcFileDataManager {
    private const val PATH = "/data/local/tmp/developerhelper"
    private val map = hashMapOf<Class<*>, Any>(
        AppXposedProcessData::class.java to AppXposedProcessDataImpl(),
        DataFinderListProcessData::class.java to DataFinderListProcessDataImpl(),
        DumpDexListProcessData::class.java to DumpDexListProcessDataImpl(),
        GlobalConfigProcessData::class.java to GlobalConfigProcessDataImpl()
    )

    fun getFile(name: String) = IpcFileInfo(File(PATH, "${name}.dat"))
    fun <T> getService(clazz: Class<T>): T? {
        return map[clazz] as? T
    }
}

