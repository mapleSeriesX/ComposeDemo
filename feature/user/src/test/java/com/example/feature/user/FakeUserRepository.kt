package com.example.feature.user

import androidx.paging.PagingData
import com.example.core.model.User
import com.example.data.user.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    private val _users = MutableStateFlow<List<User>>(emptyList())

    var shouldReturnError = false
    var errorMsg = "网络断开"

    fun setCachedUsers(users: List<User>) {
        _users.value = users
    }

    override fun observeUsers(): Flow<List<User>> = _users.asStateFlow()

    override fun observeUsersPaged(): Flow<PagingData<User>> = flowOf(PagingData.from(_users.value))

    override suspend fun refreshUsers(): Result<Unit> {
        delay(10)
        return if (shouldReturnError) {
            Result.failure(Exception(errorMsg))
        } else {
            Result.success(Unit)
        }
    }

    override fun observeUser(id: String): Flow<User?> = MutableStateFlow(null)
    override suspend fun refreshUser(id: String): Result<Unit> = Result.success(Unit)
}
