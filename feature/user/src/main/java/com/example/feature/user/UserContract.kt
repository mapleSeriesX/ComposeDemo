package com.example.feature.user

import androidx.compose.runtime.Immutable
import com.example.core.common.util.UiText
import com.example.core.model.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

// UiState：完整描述界面状态
@Immutable
data class UserUiState(
    val isLoading: Boolean = false,
    val users: ImmutableList<User> = persistentListOf(),
    val errorMessage: UiText? = null,
)

// Intent：用户操作
sealed interface UserIntent {
    data object LoadUsers : UserIntent

    data object Refresh : UserIntent

    data class OnUserClick(val userId: String) : UserIntent
}

// Effect：一次性事件（导航、Toast 等）
sealed interface UserEffect {
    data class ShowToast(val message: UiText) : UserEffect

    data class NavigateToDetail(val userId: String) : UserEffect
}
