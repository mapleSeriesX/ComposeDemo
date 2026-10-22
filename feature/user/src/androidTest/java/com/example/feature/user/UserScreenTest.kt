package com.example.feature.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.core.common.util.UiText
import com.example.core.model.User
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

class UserScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userContent_showsUserList() {
        val users = persistentListOf(
            User(id = "1", name = "Alice", url = "url1"),
            User(id = "2", name = "Bob", url = "url2")
        )

        composeTestRule.setContent {
            UserContent(
                state = UserUiState(users = users),
                onIntent = {}
            )
        }

        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
    }

    @Test
    fun userContent_showsError() {
        composeTestRule.setContent {
            UserContent(
                state = UserUiState(errorMessage = UiText.DynamicString("网络出现故障")),
                onIntent = {}
            )
        }

        composeTestRule.onNodeWithText("网络出现故障").assertIsDisplayed()
    }
}
