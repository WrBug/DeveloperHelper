package com.wrbug.developerhelper.ipcserver

import com.wrbug.developerhelper.ipc.processshare.tcp.MessageHandler
import com.wrbug.developerhelper.ipc.processshare.tcp.TcpManager
import com.wrbug.developerhelper.ipcserver.annotation.Controller

object IpcManager {
    private val map = HashMap<String, MethodInfo>()

    fun init() {
        registerController(GlobalConfigProcessDataImpl())
        registerController(AppXposedProcessDataImpl())
        registerController(DumpDexListProcessDataImpl())
        registerController(DataFinderListProcessDataImpl())
        registerController(FileProcessDataImpl())
        TcpManager.messageHandler = object : MessageHandler {
            override fun handle(action: String, message: String): String {
                if (!map.containsKey(action)) {
                    return ""
                }
                val info = map[action] ?: return ""
                info.method.run {
                    return try {
                        if (parameterTypes.isEmpty()) {
                            invoke(info.obj)?.toString() ?: ""
                        } else {
                            invoke(info.obj, message)?.toString() ?: ""
                        }
                    } catch (t: Throwable) {
                        ""
                    }
                }
            }

        }
        TcpManager.startServer()
    }

    private fun registerController(obj: Any) {
        obj.javaClass.declaredMethods.forEach {
            val router = it.getAnnotation(Controller::class.java)?.value ?: return@forEach
            map[router] = MethodInfo(obj, it)
        }
    }
}