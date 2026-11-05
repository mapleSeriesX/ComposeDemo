package com.example.feature.recognition

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import com.example.core.common.util.UiText

@Immutable
data class RecognizedItem(
    val title: String,
    val confidence: Float,
    val bitmap: Bitmap,
    val details: String,
)

@Immutable
data class RecognitionUiState(
    val isRecognizing: Boolean = false,
    val recognizedItem: RecognizedItem? = null,
    val isSaving: Boolean = false,
    val errorMessage: UiText? = null,
)

sealed interface RecognitionIntent {
    data class PerformRecognition(val bitmap: Bitmap, val rotationDegrees: Int) : RecognitionIntent

    data class SaveRecognitionResult(val title: String, val bitmap: Bitmap) : RecognitionIntent

    data object DismissResult : RecognitionIntent
}

sealed interface RecognitionEffect {
    data class ShowToast(val message: UiText) : RecognitionEffect
}
