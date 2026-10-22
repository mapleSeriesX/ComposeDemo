package com.example.core.network

import com.example.core.model.User
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResult<List<User>>

    @GET("user")
    suspend fun getUser(
        @Query("id") id: String,
    ): NetworkResult<User>
}
