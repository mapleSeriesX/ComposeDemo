package com.example.myapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.data.preferences.PreferencesRepository
import com.example.feature.recognition.RecognitionScreen
import com.example.feature.user.UserScreen
import com.example.feature.userDetail.UserDetailScreen
import com.example.feature.userDetail.UserDetailViewModel

private val topLevelDestinations = setOf(UserList, Recognition)

@Composable
fun AppNavigation(repository: PreferencesRepository) {

    val backStack = rememberNavBackStack(UserList)
    val currentStack by remember { derivedStateOf { backStack.lastOrNull() } }

    val currentTopLevelKey by remember {
        derivedStateOf {
            backStack.firstOrNull { it in topLevelDestinations } ?: UserList
        }
    }

    val isTopLevel by remember {
        derivedStateOf {
            currentStack in topLevelDestinations
        }
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()

    NavigationSuiteScaffold(
        modifier = Modifier.fillMaxSize(),
        layoutType = if (isTopLevel) {
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
        } else {
            NavigationSuiteType.None
        },
        navigationSuiteItems = {
            AppDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = stringResource(destination.contentDescription),
                        )
                    },
                    label = { Text(stringResource(destination.label)) },
                    selected = destination.key == currentTopLevelKey,
                    onClick = {
                        if (destination.key != currentTopLevelKey) {
                            backStack.clear()
                            backStack.add(destination.key)
                        }
                    },
                )
            }
        },
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<UserList> { _ ->
                    UserScreen { userId -> backStack.add(UserDetail(userId)) }
                }
                entry<UserDetail> { (userId) ->
                    val viewModel = hiltViewModel<UserDetailViewModel, UserDetailViewModel.Factory> { factory ->
                        factory.create(userId)
                    }
                    UserDetailScreen(viewModel = viewModel)
                }
                entry<Recognition> { _ ->
                    RecognitionScreen()
                }
            },
        )
    }

    LaunchedEffect(currentStack) {
        repository.setDarkMode(currentStack is DarkThemeScreen)
    }
}
