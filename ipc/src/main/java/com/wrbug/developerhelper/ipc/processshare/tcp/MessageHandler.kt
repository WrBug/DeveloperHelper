package com.wrbug.developerhelper.ipc.processshare.tcp

interface MessageHandler {
    fun handle(action: String, message: String): String
}