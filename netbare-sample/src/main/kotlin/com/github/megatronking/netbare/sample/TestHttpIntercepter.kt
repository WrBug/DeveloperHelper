package com.github.megatronking.netbare.sample

import com.github.megatronking.netbare.http.*
import java.nio.ByteBuffer

class TestHttpIntercepter : HttpInterceptor() {

    override fun intercept(chain: HttpRequestChain, buffer: ByteBuffer) {
        // 对Http请求包进行自定义处理
        // 将Http请求发射出去，交给下个拦截器或者发给服务器
        chain.process(buffer)
    }

    override fun intercept(chain: HttpResponseChain, buffer: ByteBuffer) {
        // 对Http响应包进行自定义处理
        // 将Http响应发射出去，交给下个拦截器或者发给客户端
        chain.process(buffer)
    }

    override fun onRequestFinished(request: HttpRequest) {
        // Http请求包已全部发送完成
    }

    override fun onResponseFinished(response: HttpResponse) {
        val url = response.url()
        if (url==null) {

        }
        // Http响应包已全部发送完成
    }

}