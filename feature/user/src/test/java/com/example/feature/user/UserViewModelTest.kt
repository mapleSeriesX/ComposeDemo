package com.example.feature.user

import app.cash.turbine.test
import com.example.core.common.util.UiText
import com.example.core.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {
    private lateinit var repository: FakeUserRepository
    private lateinit var viewModel: UserViewModel

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testUser = User(id = "1", name = "Tom", url = "url")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeUserRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshUsers failure with NO cache shows error and toast`() =
        runTest {
            // Given: 网络失败，无缓存
            repository.shouldReturnError = true
            repository.setCachedUsers(emptyList())

            viewModel = UserViewModel(repository)

            viewModel.effect.test {
                viewModel.uiState.test {
                    val initialState = awaitItem()
                    assertNull(initialState.errorMessage)

                    viewModel.onIntent(UserIntent.Refresh)

                    val loadingState = awaitItem()
                    assertTrue(loadingState.isLoading)

                    val finalState = awaitItem()
                    assertFalse(finalState.isLoading)
                    assertEquals(UiText.DynamicString("网络断开"), finalState.errorMessage)

                    cancelAndIgnoreRemainingEvents()
                }

                val effect = awaitItem()
                assertEquals(UserEffect.ShowToast(UiText.StringResource(R.string.user_network_error_toast)), effect)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `refreshUsers failure WITH cache clears error and shows cache`() =
        runTest {
            repository.shouldReturnError = true
            repository.setCachedUsers(listOf(testUser))

            viewModel = UserViewModel(repository)

            viewModel.effect.test {
                viewModel.uiState.test {
                    val initialState = awaitItem()
                    assertEquals(1, initialState.users.size)

                    viewModel.onIntent(UserIntent.Refresh)

                    val loadingState = awaitItem()
                    assertTrue(loadingState.isLoading)

                    val finalState = awaitItem()
                    assertFalse(finalState.isLoading)
                    assertNull(finalState.errorMessage)
                    assertEquals(1, finalState.users.size)

                    cancelAndIgnoreRemainingEvents()
                }

                val effect = awaitItem()
                assertEquals(UserEffect.ShowToast(UiText.StringResource(R.string.user_network_error_cache)), effect)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
