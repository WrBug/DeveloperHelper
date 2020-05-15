package com.wrbug.developerhelper.ipc.processshare.tcp

interface MessageReceiver {
    fun onReceived(message: String)
}