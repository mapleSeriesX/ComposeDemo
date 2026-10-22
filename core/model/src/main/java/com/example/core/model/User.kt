package com.example.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.util.UUID

@Immutable
@Serializable
data class User(
    val name: String,
    val id: String = UUID.randomUUID().toString(),
    val url: String
)
