package com.wrbug.developerhelper.ipc.processshare.tcp

import com.google.gson.annotations.SerializedName
import com.wrbug.developerhelper.commonutil.fromJson
import com.wrbug.developerhelper.commonutil.print
import com.wrbug.developerhelper.commonutil.toJson
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket

object TcpManager {
    private const val PORT = 23412
    private val tcpServer = TCPServer()
    var messageHandler: MessageHandler? = null


    fun startServer() {
        tcpServer.setOnMessageReceivedListener(object : TCPServer.OnMessageReceived {
            override fun messageReceived(message: String?, clientIndex: Int) {
                val data = message?.fromJson<TCPData>()
                if (data == null || data.action.isNullOrEmpty()) {
                    tcpServer.sendln(clientIndex, "")
                    return
                }
                "action=${data.action} data=${data.data} $clientIndex".print()
                tcpServer.sendln(
                    clientIndex,
                    messageHandler?.handle(data.action ?: "", data.data ?: "") ?: ""
                )
            }
        })
        tcpServer.startServer(PORT)
    }


    fun sendMessage(
        action: String,
        message: String
    ): Observable<String> {
        return Observable.create<String> {
            val clientSocket = Socket("localhost", PORT)
            val outToServer = DataOutputStream(clientSocket.getOutputStream())
            val inFromServer = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val data = TCPData(action, message)
            outToServer.writeBytes(data.toJson() + '\n')
            val result = inFromServer.readLine()
            clientSocket.close()
            "onReceived: $result".print()
            it.onNext(result ?: "")
        }.onErrorResumeNext {
            Observable.just("")
        }.subscribeOn(Schedulers.io())
    }

    data class TCPData(
        @SerializedName("action")
        var action: String?,
        @SerializedName("data")
        var data: String?
    )
}