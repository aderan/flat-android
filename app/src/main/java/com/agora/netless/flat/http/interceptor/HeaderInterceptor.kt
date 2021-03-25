package com.agora.netless.flat.http.interceptor

import com.agora.netless.flat.http.HeaderProvider
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor constructor(var headerProviders: Set<HeaderProvider>) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val builder = request.newBuilder().apply {
            addHeader("Content-type", "application/json; charset=utf-8")

            for (headerProvider in headerProviders) {
                for (pair in headerProvider.getHeaders()) {
                    addHeader(pair.first, pair.second)
                }
            }
        }

        return chain.proceed(builder.build())
    }
}