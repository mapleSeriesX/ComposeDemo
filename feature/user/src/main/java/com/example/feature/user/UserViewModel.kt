package com.example.feature.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.util.UiText
import com.example.core.model.User
import com.example.data.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class UserViewModel
    @Inject
    constructor(
        private val repository: UserRepository,
    ) : ViewModel() {
        // ① Room 数据源：stateIn 直接热化，Eagerly 保证 loadUsers 能读到缓存
        private val users: StateFlow<ImmutableList<User>> =
            repository.observeUsers()
                .map { it.toImmutableList() }
                .catch { e ->
                    if (e is CancellationException) throw e
                    errorMessageFlow.value = e.message?.let { UiText.DynamicString(it) }
                        ?: UiText.StringResource(R.string.user_load_failed)
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Eagerly,
                    initialValue = persistentListOf(),
                )

        private val isLoadingFlow = MutableStateFlow(false)
        private val errorMessageFlow = MutableStateFlow<UiText?>(null)

        // ② UI 状态：combine 三个源，WhileSubscribed 控制 UI 层计算
        val uiState: StateFlow<UserUiState> =
            combine(
                users,
                isLoadingFlow,
                errorMessageFlow,
            ) { users, loading, error ->
                UserUiState(users = users, isLoading = loading, errorMessage = error)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = UserUiState(),
            )

        private val _effect = Channel<UserEffect>(Channel.BUFFERED)
        val effect: Flow<UserEffect> = _effect.receiveAsFlow()

        fun onIntent(intent: UserIntent) {
            when (intent) {
                UserIntent.LoadUsers, UserIntent.Refresh -> loadUsers()
                is UserIntent.OnUserClick ->
                    viewModelScope.launch {
                        _effect.send(UserEffect.NavigateToDetail(intent.userId))
                    }
            }
        }

        private fun loadUsers() {
            viewModelScope.launch {
                isLoadingFlow.value = true
                errorMessageFlow.value = null
                repository.refreshUsers().onFailure { e ->
                    // users 是 Eagerly，这里读到的缓存一定准确
                    if (users.value.isNotEmpty()) {
                        errorMessageFlow.value = null
                        _effect.send(
                            UserEffect.ShowToast(UiText.StringResource(R.string.user_network_error_cache)),
                        )
                    } else {
                        val errorUiText =
                            e.message?.let { UiText.DynamicString(it) }
                                ?: UiText.StringResource(R.string.user_load_failed)
                        errorMessageFlow.value = errorUiText
                        _effect.send(
                            UserEffect.ShowToast(UiText.StringResource(R.string.user_network_error_toast)),
                        )
                    }
                }
                isLoadingFlow.value = false
            }
        }
    }
