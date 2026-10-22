package com.example.feature.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.common.analytics.trackClick
import com.example.core.common.util.UiText
import com.example.core.model.User
import kotlinx.collections.immutable.persistentListOf

@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasLoaded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasLoaded) {
            hasLoaded = true
            viewModel.onIntent(UserIntent.Refresh)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message.asString(context))
                }

                is UserEffect.NavigateToDetail -> {
                    onNavigateToDetail(effect.userId)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        UserContent(
            state = uiState,
            onIntent = viewModel::onIntent,
            modifier =
                Modifier
                    .padding(padding)
                    .imePadding(),
        )
    }
}

@Composable
fun UserContent(
    state: UserUiState,
    onIntent: (UserIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.errorMessage.asString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Button(
                    onClick = { onIntent(UserIntent.Refresh) },
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.user_retry))
                }
            }
        }

        else -> {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(state.users, key = { it.id }) { user ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        modifier =
                            Modifier.trackClick(
                                eventName = "user_item_click",
                                params = mapOf("user_id" to user.id),
                            ) {
                                onIntent(UserIntent.OnUserClick(user.id))
                            },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserContentPreview() {
    MaterialTheme {
        UserContent(
            state =
                UserUiState(
                    users =
                        persistentListOf(
                            User("1", "Alice", "https://example.com/1.png"),
                            User("2", "Bob", "https://example.com/2.png"),
                        ),
                ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserContentErrorPreview() {
    MaterialTheme {
        UserContent(
            state = UserUiState(errorMessage = UiText.StringResource(R.string.user_network_error_toast)),
            onIntent = {},
        )
    }
}
