package com.antony.muzei.pixiv.provider.network

import android.annotation.SuppressLint
import androidx.preference.PreferenceManager
import com.antony.muzei.pixiv.BuildConfig
import com.antony.muzei.pixiv.PixivMuzei
import com.antony.muzei.pixiv.provider.network.interceptor.NetworkTrafficLogInterceptor
import okhttp3.OkHttpClient
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

object OkHttpSingleton {
    private val x509TrustManager: X509TrustManager = object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }


    private fun OkHttpClient.Builder.logOnDebug(): OkHttpClient.Builder =
        this.apply {
            if (BuildConfig.DEBUG) {
                addNetworkInterceptor(NetworkTrafficLogInterceptor())
            }
        }

    private var instance: OkHttpClient? = null
    private var preEnableNetworkBypass:Boolean = false

    fun getInstance(): OkHttpClient {
        val enableNetworkBypass = PreferenceManager.getDefaultSharedPreferences(PixivMuzei.context?.applicationContext)
            .getBoolean("pref_enableNetworkBypass", false)
        if (instance == null || preEnableNetworkBypass != enableNetworkBypass) {
            preEnableNetworkBypass = enableNetworkBypass
            instance = OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .apply {
                    if (enableNetworkBypass) {
                        sslSocketFactory(RubySSLSocketFactory(), x509TrustManager)
                        dns(RubyHttpDns.getInstance())
                    }

                }
                //.hostnameVerifier { _, _ -> true }
                .logOnDebug()
                .build()
        }
        return instance as OkHttpClient
    }
}
