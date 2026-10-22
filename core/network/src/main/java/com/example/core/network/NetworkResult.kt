package com.example.core.network

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Error(val code: Int, val message: String) : NetworkResult<Nothing>
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>

    fun <R> map(transform: (T) -> R): NetworkResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(code, message)
        is Exception -> Exception(throwable)
    }

    suspend fun onSuccess(action: suspend (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    suspend fun onFailure(action: suspend (code: Int, message: String) -> Unit): NetworkResult<T> {
        when (this) {
            is Error -> action(code, message)
            is Exception -> action(-1, throwable.localizedMessage ?: "Unknown Network Error")
            is Success -> {}
        }
        return this
    }
}

inline fun <T> NetworkResult<T>.getOrNull(): T? = when (this) {
    is NetworkResult.Success -> data
    else -> null
}

inline fun <T> NetworkResult<T>.getOrDefault(defaultValue: T): T = when (this) {
    is NetworkResult.Success -> data
    else -> defaultValue
}
