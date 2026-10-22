package com.example.core.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class AbTestExperiment(
    val key: String,
    val variantName: String,
    val isEnabled: Boolean = true,
    val payload: Map<String, String> = emptyMap(),
)
