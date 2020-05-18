package com.wrbug.developerhelper.ipc.processshare

import com.wrbug.developerhelper.commonutil.toJson
import com.wrbug.developerhelper.ipc.processshare.annotation.Url
import com.wrbug.developerhelper.ipc.processshare.tcp.TcpManager
import io.reactivex.rxjava3.core.Observable
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class ProcessDataInvocationHandler : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        method?.let {
            val annotation = it.getAnnotation(Url::class.java) ?: throw Exception("@Url")
            val url = annotation.value
            val message: String = if (args.isNullOrEmpty()) {
                ""
            } else {
                when (val data = args[0]) {
                    is Boolean, Int, Long, String, Float, Double -> data.toString()
                    else -> data.toJson() ?: ""
                }
            }
            val result = TcpManager.sendMessage(url, message)
            if (method.returnType == Observable::class.java) {
                return result
            } else {
                result.subscribe({}, {})
            }
        }
        return null
    }
}