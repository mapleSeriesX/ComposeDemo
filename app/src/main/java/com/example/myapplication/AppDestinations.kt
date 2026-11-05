package com.example.myapplication

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

enum class AppDestinations(
    @StringRes val label: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int,
    val key: NavKey,
) {
    HOME(R.string.home, Icons.Default.Home, R.string.home,UserList),
    FAVORITES(R.string.favorites, Icons.Default.Favorite, R.string.favorites,Recognition),
}