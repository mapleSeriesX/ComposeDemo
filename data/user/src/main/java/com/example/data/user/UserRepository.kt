package com.example.data.user

import androidx.paging.PagingData
import com.example.core.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUsers(): Flow<List<User>>

    fun observeUsersPaged(): Flow<PagingData<User>>

    suspend fun refreshUsers(): Result<Unit>

    fun observeUser(id: String): Flow<User?>

    suspend fun refreshUser(id: String): Result<Unit>
}
