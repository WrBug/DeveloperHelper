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
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.Reader
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/**
 * 注入器范例2：修改发朋友圈定位
 *
 * 启动NetBare服务后，打开朋友圈发状态->所在位置，会发现POI都变成被修改地点附近的。
 *
 * @author Megatron King
 * @since 2019/1/2 22:17
 */
class WechatLocationInjector : SimpleHttpInjector() {

    companion object {
        const val TAG = "WechatLocationInjector"
    }

    private var mHoldResponseHeader: HttpResponseHeaderPart? = null

    override fun sniffResponse(response: HttpResponse): Boolean {
        // 请求url匹配时才进行注入
        val shouldInject = response.url().startsWith("https://lbs.map.qq.com/loc")
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
        var reader: Reader? = null
        var fos: DeflaterOutputStream? = null
        var bos: ByteArrayOutputStream? = null
        try {
            his = HttpBodyInputStream(body)
            // 数据使用了zlib编码，这里需要先解码
            reader = InputStreamReader(InflaterInputStream(his))
            val element = JsonParser().parse(reader)
            if (element == null || !element.isJsonObject) {
                return
            }
            val locationNode = element.asJsonObject.get("location")
            if (locationNode == null || !locationNode.isJsonObject) {
                return
            }
            // 替换经纬度，这里是珠峰的经纬度，装逼很厉害的地方
            val location = locationNode.asJsonObject
            location.addProperty("latitude", 27.99136f)
            location.addProperty("longitude", 86.88915f)
            val injectedBody = element.toString()
            // 重新使用zlib编码
            bos = ByteArrayOutputStream()
            fos = DeflaterOutputStream(bos)
            fos.write(injectedBody.toByteArray())
            fos.finish()
            fos.flush()
            val injectBodyData = bos.toByteArray()
            // 更新header的content-length
            val injectHeader = mHoldResponseHeader!!
                    .newBuilder()
                    .replaceHeader("Content-Length", injectBodyData.size.toString())
                    .build()
            // 先将header发射出去
            callback.onFinished(injectHeader)
            // 再将响应体发射出去
            callback.onFinished(ByteStream(injectBodyData))
            Log.i(TAG, "Inject wechat location completed!")
        } finally {
            NetBareUtils.closeQuietly(his)
            NetBareUtils.closeQuietly(reader)
            NetBareUtils.closeQuietly(fos)
            NetBareUtils.closeQuietly(bos)
        }
        mHoldResponseHeader = null
    }

}
