package com.example.core.network

import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class NetworkResultCallAdapter<R>(
    private val successType: Type
) : CallAdapter<R, Call<NetworkResult<R>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<R>): Call<NetworkResult<R>> {
        return NetworkResultCall(call)
    }
}

private class NetworkResultCall<R>(
    private val delegate: Call<R>
) : Call<NetworkResult<R>> {

    override fun enqueue(callback: Callback<NetworkResult<R>>) {
        delegate.enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                val body = response.body()
                val code = response.code()
                val errorBody = response.errorBody()

                if (response.isSuccessful) {
                    if (body != null) {
                        callback.onResponse(
                            this@NetworkResultCall,
                            Response.success(NetworkResult.Success(body))
                        )
                    } else {
                        // 2xx 但无响应体
                        callback.onResponse(
                            this@NetworkResultCall,
                            Response.success(NetworkResult.Error(code, "Response body is null"))
                        )
                    }
                } else {
                    val msg = errorBody?.string() ?: response.message()
                    callback.onResponse(
                        this@NetworkResultCall,
                        Response.success(NetworkResult.Error(code, msg))
                    )
                }
            }

            override fun onFailure(call: Call<R>, t: Throwable) {
                callback.onResponse(
                    this@NetworkResultCall,
                    Response.success(NetworkResult.Exception(t))
                )
            }
        })
    }

    override fun clone(): Call<NetworkResult<R>> = NetworkResultCall(delegate.clone())

    override fun execute(): Response<NetworkResult<R>> {
        throw UnsupportedOperationException("NetworkResultCall doesn't support execute()")
    }

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}

class NetworkResultCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != NetworkResult::class.java) {
            return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return NetworkResultCallAdapter<Any>(resultType)
    }

    companion object {
        fun create(): NetworkResultCallAdapterFactory = NetworkResultCallAdapterFactory()
    }
}
