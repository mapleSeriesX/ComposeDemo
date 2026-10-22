package com.example.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class ErrorInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val response: Response
            try {
                response = chain.proceed(request)
            } catch (e: IOException) {
                throw NetworkException.NoInternetException("网络异常或连接超时", e)
            }

            if (!response.isSuccessful) {
                when (response.code) {
                    401, 403 -> throw NetworkException.AuthException("身份验证机制异常 (${response.code})")
                    404 -> throw NetworkException.NotFoundException("请求资源不存在 (${response.code})")
                    in 500..599 -> throw NetworkException.ServerException("服务器内部错误 (${response.code})")
                    else -> throw NetworkException.HttpException("网络请求失败 (${response.code})")
                }
            }
            return response
        }
    }

sealed class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    class NoInternetException(message: String, cause: Throwable? = null) : NetworkException(message, cause)

    class AuthException(message: String) : NetworkException(message)

    class NotFoundException(message: String) : NetworkException(message)

    class ServerException(message: String) : NetworkException(message)

    class HttpException(message: String) : NetworkException(message)
}
