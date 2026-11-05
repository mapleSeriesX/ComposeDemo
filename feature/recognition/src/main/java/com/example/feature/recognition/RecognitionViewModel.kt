package com.example.feature.recognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.util.UiText
import com.example.core.model.User
import com.example.data.user.UserRepository
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecognitionViewModel
    @Inject
    constructor(
        private val repository: UserRepository,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {

        private val _uiState = MutableStateFlow(RecognitionUiState())
        val uiState: StateFlow<RecognitionUiState> = _uiState.asStateFlow()

        private val _effect = Channel<RecognitionEffect>(Channel.BUFFERED)
        val effect: Flow<RecognitionEffect> = _effect.receiveAsFlow()

        private val detector: ObjectDetector = createObjectDetector(context)

        fun onIntent(intent: RecognitionIntent) {
            when (intent) {
                is RecognitionIntent.PerformRecognition -> processImage(intent.bitmap, intent.rotationDegrees)
                is RecognitionIntent.SaveRecognitionResult -> saveResult(intent.title, intent.bitmap)
                RecognitionIntent.DismissResult -> _uiState.update { it.copy(recognizedItem = null) }
            }
        }

        private fun createObjectDetector(context: Context): ObjectDetector {
            val assetFileName = "food.tflite"
            val hasAssetModel = try {
                context.assets.open(assetFileName).close()
                true
            } catch (e: Exception) {
                false
            }

            return if (hasAssetModel) {
                val localModel = LocalModel.Builder()
                    .setAssetFilePath(assetFileName)
                    .build()
                val customOptions = CustomObjectDetectorOptions.Builder(localModel)
                    .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()
                    .setMaxPerObjectLabelCount(3)
                    .setClassificationConfidenceThreshold(0.3f)
                    .build()
                ObjectDetection.getClient(customOptions)
            } else {
                val defaultOptions = ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()
                    .build()
                ObjectDetection.getClient(defaultOptions)
            }
        }

        private fun processImage(bitmap: Bitmap, rotationDegrees: Int) {
            viewModelScope.launch {
                _uiState.update { it.copy(isRecognizing = true, errorMessage = null) }

                val rotatedBitmap = if (rotationDegrees != 0) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else {
                    bitmap
                }

                val inputImage = InputImage.fromBitmap(rotatedBitmap, 0)

                detector.process(inputImage)
                    .addOnSuccessListener { detectedObjects ->
                        var bestTitle = "识别目标"
                        var bestConfidence = 0f

                        for (obj in detectedObjects) {
                            val labels = obj.labels
                            if (labels.isNotEmpty()) {
                                val topLabel = labels.maxByOrNull { it.confidence }
                                if (topLabel != null) {
                                    bestTitle = topLabel.text
                                    bestConfidence = topLabel.confidence
                                    break
                                }
                            }
                        }


                        if(bestConfidence==0f){
                            _uiState.update {
                                it.copy(
                                    isRecognizing = false,
                                    errorMessage = UiText.DynamicString( "识别失败"),
                                )
                            }
                            viewModelScope.launch {
                                _effect.send(RecognitionEffect.ShowToast(UiText.DynamicString("识别失败")))
                            }
                        }else{
                            val confidencePct = (bestConfidence * 100).toInt()
                            val item = RecognizedItem(
                                title = bestTitle,
                                confidence = bestConfidence,
                                bitmap = rotatedBitmap,
                                details = "识别结果: $bestTitle\n置信度: $confidencePct%\n通过 ML Kit 视觉算法模型分析得出。",
                            )
                            _uiState.update { it.copy(isRecognizing = false, recognizedItem = item) }
                        }

                    }
                    .addOnFailureListener { exc ->
                        _uiState.update {
                            it.copy(
                                isRecognizing = false,
                                errorMessage = UiText.DynamicString(exc.localizedMessage ?: "识别失败"),
                            )
                        }
                    }
            }
        }

        private fun saveResult(title: String, bitmap: Bitmap) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSaving = true) }
                try {
                    val savedPath = saveBitmapToLocalStorage(bitmap)
                    val newRecord = User(
                        id = UUID.randomUUID().toString(),
                        name = title,
                        url = savedPath,
                    )
                    val result = repository.saveRecord(newRecord)
                    if (result.isSuccess) {
                        _uiState.update { it.copy(isSaving = false, recognizedItem = null) }
                        _effect.send(RecognitionEffect.ShowToast(UiText.DynamicString("已保存至历史记录")))
                    } else {
                        _uiState.update { it.copy(isSaving = false) }
                        _effect.send(RecognitionEffect.ShowToast(UiText.DynamicString("保存失败")))
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isSaving = false) }
                    _effect.send(RecognitionEffect.ShowToast(UiText.DynamicString("保存失败: ${e.localizedMessage}")))
                }
            }
        }

        private suspend fun saveBitmapToLocalStorage(bitmap: Bitmap): String =
            withContext(Dispatchers.IO) {
                val photosDir = File(context.filesDir, "recognition_photos")
                if (!photosDir.exists()) {
                    photosDir.mkdirs()
                }
                val photoFile = File(photosDir, "recognition_${System.currentTimeMillis()}.jpg")
                FileOutputStream(photoFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                photoFile.absolutePath
            }
    }
