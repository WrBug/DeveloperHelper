package com.wrbug.developerhelper.ipc.processshare.tcp

import com.wrbug.developerhelper.commonutil.print
import org.jetbrains.anko.doAsync
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress
import java.util.*

class TCPServer {
    private var mMessageListener: OnMessageReceived? = null
    private var mConnectListener: OnConnect? = null
    private var mDisconnectListener: OnDisconnect? = null
    private var mServerClosedListener: OnServerClose? = null
    private var mServerStartListener: OnServerStart? = null
    private var serverSocket: ServerSocket? = null
    private var lastClientIndex: Short = 0
    private val clients: MutableMap<Int, Client> = HashMap()
    var isServerRunning = false
        private set

    fun startServer(port: String?) {
        startServer(Integer.valueOf(port))
    }

    fun startServer(port: Int) {
        doAsync {

            isServerRunning = true
            var socket: Socket? = null
            try {
                serverSocket = ServerSocket(port)
            } catch (e: IOException) {
                e.printStackTrace()
                isServerRunning = false
            }
            ("startServer: " + (mServerStartListener != null)).print()
            if (mServerStartListener != null) {
                mServerStartListener!!.serverStarted(port)
            }
            while (isServerRunning) {
                "Accepting client".print()
                try {
                    socket = serverSocket!!.accept()
                    val client = Client(socket)
                    lastClientIndex++
                    clients[lastClientIndex.toInt()] = client
                    Thread(client).start()
                    client.setIndex(lastClientIndex.toInt())
                    if (mConnectListener != null) {
                        mConnectListener!!.connected(
                            socket,
                            socket.localAddress,
                            +socket.localPort,
                            socket.localSocketAddress,
                            lastClientIndex.toInt()
                        )
                    }
                } catch (e: IOException) {
                    isServerRunning = false
                    break
                }
            }
            if (mServerClosedListener != null) {
                mServerClosedListener!!.serverClosed(port)
            }

        }
    }

    fun closeServer() {
        try {
            "closeServer: ".print()
            isServerRunning = false
            serverSocket!!.close()
            kickAll()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun kickAll() {
        for (client in clients.values) {
            client.kill()
        }
    }

    fun kick(clientIndex: Int) {
        clients[clientIndex]!!.kill()
    }

    fun sendln(clientIndex: Int, message: String?) {
        clients[clientIndex]?.output?.run {
            println(message)
            flush()
        }
    }

    fun send(clientIndex: Int, message: String?) {
        clients[clientIndex]?.output?.run {
            print(message)
            flush()
        }
    }

    fun broadcast(message: String?) {
        for (client in clients.values) {
            client.output?.run {
                print(message)
                flush()
            }
        }
    }

    fun broadcastln(message: String?) {
        for (client in clients.values) {
            client.output?.run {
                println(message)
                flush()
            }
        }
    }

    fun getClients(): Map<Int, Client> {
        return clients
    }

    val clientsCount: Int
        get() = clients.size

    //---------------------------------------------[Listeners]----------------------------------------------//
    fun setOnMessageReceivedListener(listener: OnMessageReceived?) {
        mMessageListener = listener
    }

    fun setOnConnectListener(listener: OnConnect?) {
        mConnectListener = listener
    }

    fun setOnDisconnectListener(listener: OnDisconnect?) {
        mDisconnectListener = listener
    }

    fun setOnServerClosedListener(listener: OnServerClose?) {
        mServerClosedListener = listener
    }

    fun setOnServerStartListener(listener: OnServerStart?) {
        mServerStartListener = listener
    }

    //---------------------------------------------[Interfaces]---------------------------------------------//
    interface OnMessageReceived {
        fun messageReceived(message: String?, clientIndex: Int)
    }

    interface OnConnect {
        fun connected(
            socket: Socket?,
            localAddress: InetAddress?,
            port: Int,
            localSocketAddress: SocketAddress?,
            clientIndex: Int
        )
    }

    interface OnDisconnect {
        fun disconnected(
            socket: Socket?,
            localAddress: InetAddress?,
            port: Int,
            localSocketAddress: SocketAddress?,
            clientIndex: Int
        )
    }

    interface OnServerClose {
        fun serverClosed(port: Int)
    }

    interface OnServerStart {
        fun serverStarted(port: Int)
    }

    //--------------------------------------------[Client class]--------------------------------------------//
    inner class Client(private val socket: Socket) : Runnable {
        var output: PrintWriter? = null
        private var input: BufferedReader? = null
        private var clientIndex = 0
        override fun run() {
            while (isServerRunning) {
                "Read line (Client: $clientIndex)".print()
                try {
                    val line = input!!.readLine()
                    println(line)
                    if (mMessageListener != null) {
                        if (line == null) {
                            socket.close()
                            clients.remove(clientIndex)
                            if (mDisconnectListener != null) {
                                mDisconnectListener!!.disconnected(
                                    socket,
                                    socket.localAddress,
                                    +socket.localPort,
                                    socket.localSocketAddress,
                                    clientIndex
                                )
                            }
                            break
                        } else {
                            mMessageListener!!.messageReceived(line, clientIndex)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        fun kill() {
            try {
                socket.shutdownInput()
            } catch (e: Exception) {
            }
            try {
                socket.shutdownOutput()
            } catch (e: Exception) {
            }
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun setIndex(index: Int) {
            clientIndex = index
        }

        init {
            try {
                input = BufferedReader(InputStreamReader(socket.getInputStream()))
                output =
                    PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}