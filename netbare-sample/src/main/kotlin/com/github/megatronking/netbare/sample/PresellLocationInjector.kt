package com.github.megatronking.netbare.sample

import android.util.Log
import com.github.megatronking.netbare.NetBareUtils
import com.github.megatronking.netbare.http.HttpBody
import com.github.megatronking.netbare.http.HttpResponse
import com.github.megatronking.netbare.http.HttpResponseHeaderPart
import com.github.megatronking.netbare.injector.InjectorCallback
import com.github.megatronking.netbare.injector.SimpleHttpInjector
import com.github.megatronking.netbare.io.HttpBodyInputStream
import com.github.megatronking.netbare.stream.ByteStream
import com.google.gson.JsonParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.util.*
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

/**
 * 注入器范例2：修改发朋友圈定位
 *
 * 启动NetBare服务后，打开朋友圈发状态->所在位置，会发现POI都变成被修改地点附近的。
 *
 * @author Sundy
 * @since 2019/1/2 22:17
 */
class PresellLocationInjector : SimpleHttpInjector() {

    companion object {
        const val TAG = "WechatLocationInjector"
    }

    private var mHoldResponseHeader: HttpResponseHeaderPart? = null

    override fun sniffResponse(response: HttpResponse): Boolean {
        // 请求url匹配时才进行注入
        val shouldInject = response.url().startsWith("http://boss-apk.10.1.133.14.xip.io/")
        if (shouldInject) {
            Log.i(TAG, "Start wechat location injection!")
        }
        return shouldInject
    }

    override fun onResponseInject(header: HttpResponseHeaderPart, callback: InjectorCallback) {
        // 由于响应体大小不确定，这里先hold住header（需要在后面修改content-length）
        mHoldResponseHeader = header
    }

    override fun onResponseInject(response: HttpResponse, body: HttpBody, callback: InjectorCallback) {
        if (mHoldResponseHeader == null) {
            // 一般不会发生
            return
        }
        var his: HttpBodyInputStream? = null
        try {
//            his = HttpBodyInputStream(body)
//            var bytes: ByteArray = ByteArray(his.available())
//            his.read(bytes)
//            val str = String(bytes)
            val str = String(uncompress(body.toBuffer().array())!!)
            Log.i(TAG, str)
        } finally {
            NetBareUtils.closeQuietly(his)
        }
        mHoldResponseHeader = null
        super.onResponseInject(response, body, callback)
    }

    fun getString(buffer: ByteBuffer?): String {
        var charset: Charset? = null
        var decoder: CharsetDecoder? = null
        var charBuffer: CharBuffer? = null
        try {
            charset = Charset.forName("UTF-8")
            decoder = charset!!.newDecoder()
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder!!.decode(buffer?.asReadOnlyBuffer())
            return charBuffer!!.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            return ""
        }

    }

    fun uncompress(bytes: ByteArray?): ByteArray? {
        if (bytes == null || bytes.isEmpty()) {
            return null
        }
        val out = ByteArrayOutputStream()
        val `in` = ByteArrayInputStream(bytes)
        try {
            val ungzip = GZIPInputStream(`in`)
            val buffer = ByteArray(256)
            var n: Int = ungzip.read(buffer)
            while (n >= 0) {
                out.write(buffer, 0, n)
                n = ungzip.read(buffer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return out.toByteArray()
    }


}
