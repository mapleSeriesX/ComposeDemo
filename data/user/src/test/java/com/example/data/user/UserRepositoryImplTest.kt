package com.example.data.user

import com.example.core.database.AppDatabase
import com.example.core.database.UserDao
import com.example.core.database.UserEntity
import com.example.core.model.User
import com.example.core.network.ApiService
import com.example.core.network.NetworkResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class UserRepositoryImplTest {

    private val apiService: ApiService = mockk()
    private val userDao: UserDao = mockk(relaxed = true)
    private val database: AppDatabase = mockk(relaxed = true)

    private lateinit var repository: UserRepositoryImpl

    @Before
    fun setup() {
        repository = UserRepositoryImpl(apiService, userDao, database)
    }

    @Test
    fun `observeUsers maps Entity to Domain correctly`() = runTest {
        val entities = listOf(
            UserEntity(id = "1", name = "Alice", url = "https://example.com/1.png")
        )
        every { userDao.observeUsers() } returns flowOf(entities)

        val users = repository.observeUsers().first()

        assertEquals(1, users.size)
        assertEquals("1", users[0].id)
        assertEquals("Alice", users[0].name)
    }

    @Test
    fun `refreshUsers success inserts users into userDao`() = runTest {
        val networkUsers = listOf(
            User(id = "1", name = "Alice", url = "https://example.com/1.png")
        )
        coEvery { apiService.getUsers() } returns NetworkResult.Success(networkUsers)

        val result = repository.refreshUsers()

        assertTrue(result.isSuccess)
        coVerify { userDao.insertAll(listOf(UserEntity("1", "Alice", "https://example.com/1.png"))) }
    }

    @Test
    fun `refreshUsers network error returns failure result`() = runTest {
        coEvery { apiService.getUsers() } returns NetworkResult.Error(500, "Server Error")

        val result = repository.refreshUsers()

        assertTrue(result.isFailure)
        assertEquals("HTTP 500: Server Error", result.exceptionOrNull()?.message)
    }

    @Test(expected = CancellationException::class)
    fun `refreshUsers rethrows CancellationException to preserve coroutine scope`() = runTest {
        coEvery { apiService.getUsers() } returns NetworkResult.Exception(CancellationException("Cancelled"))

        repository.refreshUsers()
    }
}
