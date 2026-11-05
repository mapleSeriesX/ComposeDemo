package com.example.myapplication

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface DarkThemeScreen

@Serializable
data object UserList : NavKey

@Serializable
data class UserDetail(val userId: String) : NavKey, DarkThemeScreen

@Serializable
data object Recognition : NavKey
