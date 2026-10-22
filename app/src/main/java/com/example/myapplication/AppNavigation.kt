package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.data.preferences.PreferencesRepository
import com.example.feature.user.UserScreen
import com.example.feature.userDetail.UserDetailScreen
import com.example.feature.userDetail.UserDetailViewModel

@Composable
fun AppNavigation(repository: PreferencesRepository) {
    val backStack =
        rememberSaveable(
            saver =
                Saver(
                    save = { it.toList() },
                    restore = { (it as? List<*>)?.filterIsInstance<NavKey>()?.toMutableStateList() ?: mutableStateListOf(UserList) },
                ),
        ) {
            mutableStateListOf<NavKey>(UserList)
        }
    val currentStack by remember { derivedStateOf { backStack.lastOrNull() } }

    LaunchedEffect(currentStack) {
        repository.setDarkMode(currentStack is DarkThemeScreen)
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<UserList> {
                    UserScreen(
                        onNavigateToDetail = { userId ->
                            backStack.add(UserDetail(userId))
                        },
                    )
                }

                entry<UserDetail> { key ->
                    val viewModel =
                        hiltViewModel<UserDetailViewModel, UserDetailViewModel.Factory>(
                            creationCallback = { factory -> factory.create(key.userId) },
                        )
                    UserDetailScreen(viewModel = viewModel)
                }
            },
    )
}
