package com.github.megatronking.netbare.sample

import android.util.Log
import com.github.megatronking.netbare.http.HttpBody
import com.github.megatronking.netbare.http.HttpResponse
import com.github.megatronking.netbare.http.HttpResponseHeaderPart
import com.github.megatronking.netbare.injector.InjectorCallback
import com.github.megatronking.netbare.injector.SimpleHttpInjector
import com.github.megatronking.netbare.stream.BufferStream
import java.nio.ByteBuffer

/**
 * 注入器范例1：替换百度首页logo
 *
 * 启动NetBare服务后，用浏览器App打开百度首页，Logo将会被替换成此sample项目raw目录下的图片。
 * 注意：如果浏览器有图片缓存，记得先把缓存清理掉！
 *
 * @author Megatron King
 * @since 2018/12/30 00:18
 */
class BaiduLogoInjector : SimpleHttpInjector() {

    companion object {
        const val TAG = "BaiduLogoInjector"
    }

    override fun sniffResponse(response: HttpResponse): Boolean {
        // 请求url匹配时才进行注入
        val shouldInject = "https://m.baidu.com/static/index/plus/plus_logo.png".equals(
                response.url())
        if (shouldInject) {
            Log.i(TAG, "Start Miss. Du logo injection!")
        }
        return shouldInject
    }

    override fun onResponseInject(header: HttpResponseHeaderPart, callback: InjectorCallback) {
        // 响应体大小变化，一定要先更新header中的content-length
        val newHeader = header.newBuilder()
                .replaceHeader("content-length", "10764")
                .build()
        callback.onFinished(newHeader)
        Log.i(TAG, "Inject header completed!")
    }

    override fun onResponseInject(response: HttpResponse, body: HttpBody, callback: InjectorCallback) {
        // 替换图片请求响应体
        val injectIOStream = App.getInstance().resources.openRawResource(R.raw.baidu_inject_logo)
        val injectStream = BufferStream(ByteBuffer.wrap(injectIOStream.readBytes()))
        injectIOStream.close()
        callback.onFinished(injectStream)
        Log.i(TAG, "Inject body completed!")
    }

}