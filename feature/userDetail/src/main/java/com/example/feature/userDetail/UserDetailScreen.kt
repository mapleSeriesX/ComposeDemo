package com.example.feature.userDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.core.common.util.UiText
import com.example.core.model.User

@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is UserDetailEffect.ShowToast ->
                    snackbarHostState.showSnackbar(effect.message.asString(context))
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        UserDetailContent(
            state = uiState,
            onIntent = viewModel::onIntent,
            modifier =
                Modifier
                    .padding(padding)
                    .imePadding(),
        )
        User()
    }
}

@Composable
fun User() {
    Text(stringResource(R.string.user_detail_retry))
}

@Composable
fun UserDetailContent(
    state: UserDetailUiState,
    onIntent: (UserDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    onClick = { onIntent(UserDetailIntent.Retry) },
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text(stringResource(R.string.user_detail_retry))
                }
            }
        }
        state.user != null -> {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.user_detail_id, state.user.id),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.user_detail_name, state.user.name),
                    style = MaterialTheme.typography.bodyLarge,
                )
                AsyncImage(
                    model = state.user.url,
                    contentDescription = stringResource(R.string.user_detail_avatar_desc),
                    modifier = Modifier.size(120.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserDetailContentPreview() {
    MaterialTheme {
        UserDetailContent(
            state =
                UserDetailUiState(
                    user = User("1001", "Alice", "https://example.com/avatar.png"),
                ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserDetailContentErrorPreview() {
    MaterialTheme {
        UserDetailContent(
            state =
                UserDetailUiState(
                    errorMessage = UiText.StringResource(R.string.user_detail_load_failed_default),
                ),
            onIntent = {},
        )
    }
}
