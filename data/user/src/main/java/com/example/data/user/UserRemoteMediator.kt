package com.example.data.user

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.core.database.AppDatabase
import com.example.core.database.UserDao
import com.example.core.database.UserEntity
import com.example.core.network.ApiService
import com.example.core.network.NetworkResult
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class UserRemoteMediator(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val database: AppDatabase
) : RemoteMediator<Int, UserEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, UserEntity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val lastItem = state.lastItemOrNull()
                    if (lastItem == null) {
                        1
                    } else {
                        state.pages.size + 1
                    }
                }
            }

            when (val response = apiService.getUsers()) {
                is NetworkResult.Success -> {
                    val remoteUsers = response.data
                    val endOfPaginationReached = remoteUsers.isEmpty()

                    database.withTransaction {
                        if (loadType == LoadType.REFRESH) {
                            userDao.clearAll()
                        }
                        userDao.insertAll(remoteUsers.map { it.toEntity() })
                    }

                    MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
                }
                is NetworkResult.Error -> {
                    MediatorResult.Error(IOException("HTTP ${response.code}: ${response.message}"))
                }
                is NetworkResult.Exception -> {
                    MediatorResult.Error(response.throwable)
                }
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
