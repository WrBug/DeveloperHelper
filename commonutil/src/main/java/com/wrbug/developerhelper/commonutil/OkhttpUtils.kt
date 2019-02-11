package com.wrbug.developerhelper.commonutil


import okhttp3.Interceptor
import okhttp3.OkHttpClient

import javax.net.ssl.*
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

object OkhttpUtils {
    private val CONNECT_TIMEOUT_SECOND: Long = 5

    private var sOkHttpClient: OkHttpClient? = null


    val client: OkHttpClient by lazy {
        getClientFollowRedirects(true)
    }

    fun getClientFollowRedirects(follow: Boolean): OkHttpClient {
        val mBuilder = OkHttpClient.Builder()
        mBuilder.followRedirects(follow).followSslRedirects(follow)
        mBuilder.connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        return getClient(mBuilder)
    }

    fun getProxyClient(proxy: Proxy?): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(createSSLSocketFactory()!!, TrustAllManager())
        builder.hostnameVerifier(TrustAllHostnameVerifier())
        if (proxy != null) {
            builder.proxy(proxy)
        }
        return builder.build()
    }

    fun getClient(builder: OkHttpClient.Builder): OkHttpClient {
        builder.sslSocketFactory(createSSLSocketFactory()!!, TrustAllManager())
        builder.hostnameVerifier(TrustAllHostnameVerifier())
        return builder.build()
    }


    fun createSSLSocketFactory(): SSLSocketFactory? {

        var sSLSocketFactory: SSLSocketFactory? = null

        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(
                null, arrayOf<TrustManager>(TrustAllManager()),
                SecureRandom()
            )
            sSLSocketFactory = sc.socketFactory
        } catch (e: Exception) {
        }

        return sSLSocketFactory
    }


    private class TrustAllManager : X509TrustManager {
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    private class TrustAllHostnameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }
}
