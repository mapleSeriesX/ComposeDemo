package com.example.myapplication

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

interface DarkThemeScreen

@Serializable
@Parcelize
data object UserList : NavKey, Parcelable

@Serializable
@Parcelize
data class UserDetail(val userId: String) : NavKey, DarkThemeScreen, Parcelable
