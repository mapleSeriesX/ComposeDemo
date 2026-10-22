package com.example.data.user

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.core.database.AppDatabase
import com.example.core.database.UserDao
import com.example.core.model.User
import com.example.core.network.ApiService
import com.example.core.network.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImpl
    @Inject
    constructor(
        private val apiService: ApiService,
        private val userDao: UserDao,
        private val database: AppDatabase,
    ) : UserRepository {
        override fun observeUsers(): Flow<List<User>> =
            userDao.observeUsers().map { entities ->
                entities.map { it.toDomain() }
            }

        @OptIn(ExperimentalPagingApi::class)
        override fun observeUsersPaged(): Flow<PagingData<User>> =
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                remoteMediator = UserRemoteMediator(apiService, userDao, database),
                pagingSourceFactory = { userDao.pagingSource() },
            ).flow.map { pagingData ->
                pagingData.map { it.toDomain() }
            }

        override suspend fun refreshUsers(): Result<Unit> =
            when (val result = apiService.getUsers()) {
                is NetworkResult.Success -> {
                    userDao.insertAll(result.data.map { it.toEntity() })
                    Result.success(Unit)
                }
                is NetworkResult.Error -> {
                    Result.failure(Exception("HTTP ${result.code}: ${result.message}"))
                }
                is NetworkResult.Exception -> {
                    if (result.throwable is CancellationException) throw result.throwable
                    Result.failure(result.throwable)
                }
            }

        override fun observeUser(id: String): Flow<User?> = userDao.observeUser(id).map { entity -> entity?.toDomain() }

        override suspend fun refreshUser(id: String): Result<Unit> =
            when (val result = apiService.getUser(id)) {
                is NetworkResult.Success -> {
                    userDao.insertUser(result.data.toEntity())
                    Result.success(Unit)
                }
                is NetworkResult.Error -> {
                    Result.failure(Exception("HTTP ${result.code}: ${result.message}"))
                }
                is NetworkResult.Exception -> {
                    if (result.throwable is CancellationException) throw result.throwable
                    Result.failure(result.throwable)
                }
            }
    }
