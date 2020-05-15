import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.Socket

object TCPClient {
    private var clientSocket: Socket? = null
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        println("请输入要转换的字符串:")
        clientSocket = Socket("localhost", 23456)
        val outToServer = DataOutputStream(clientSocket!!.getOutputStream())
        val inFromServer = BufferedReader(InputStreamReader(clientSocket!!.getInputStream()))
        for (i in 5 downTo 1) {
            outToServer.writeBytes("123456" + '\n')
            println("FROM SERVER:" + inFromServer.readLine())
        }
        clientSocket!!.close()
    }
}