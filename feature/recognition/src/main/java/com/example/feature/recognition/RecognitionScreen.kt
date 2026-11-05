package com.example.feature.recognition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume

suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                continuation.resume(future.get())
            } catch (e: Exception) {
                continuation.resumeWith(Result.failure(e))
            }
        }, ContextCompat.getMainExecutor(context))
    }

@Composable
fun RecognitionScreen(
    viewModel: RecognitionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RecognitionEffect.ShowToast -> {
                    Toast.makeText(context, effect.message.asString(context), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted: Boolean ->
            hasCameraPermission = granted
        },
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreviewContent(
            uiState = uiState,
            onIntent = viewModel::onIntent,
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "需要相机权限来进行实时物品识别")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text(text = "授予相机权限")
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewContent(
    uiState: RecognitionUiState,
    onIntent: (RecognitionIntent) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var surfaceRequest by remember { mutableStateOf<SurfaceRequest?>(null) }
    var shouldCaptureNextFrame by remember { mutableStateOf(false) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    val preview = remember {
        Preview.Builder().build().apply {
            setSurfaceProvider { request ->
                surfaceRequest = request
            }
        }
    }

    LaunchedEffect(preview, lifecycleOwner) {
        val cameraProvider = getCameraProvider(context)
        cameraProvider.unbindAll()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            if (shouldCaptureNextFrame) {
                shouldCaptureNextFrame = false
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val bitmap: Bitmap? = try {
                    imageProxy.toBitmap()
                } catch (e: Exception) {
                    null
                }
                if (bitmap != null) {
                    onIntent(RecognitionIntent.PerformRecognition(bitmap, rotationDegrees))
                }
            }
            imageProxy.close()
        }

        try {
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        surfaceRequest?.let { request ->
            CameraXViewfinder(
                surfaceRequest = request,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // 底部“识别”按钮
        Button(
            onClick = { shouldCaptureNextFrame = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            enabled = !uiState.isRecognizing && uiState.recognizedItem == null,
        ) {
            Text(text = if (uiState.isRecognizing) "识别中..." else "识别")
        }

        if (uiState.isRecognizing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.errorMessage?.let { item ->


        }

        // 识别成功弹窗
        uiState.recognizedItem?.let { item ->
            AlertDialog(
                onDismissRequest = { onIntent(RecognitionIntent.DismissResult) },
                title = { Text(text = "识别结果: ${item.title}") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            bitmap = item.bitmap.asImageBitmap(),
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = item.details,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onIntent(RecognitionIntent.SaveRecognitionResult(item.title, item.bitmap))
                        },
                        enabled = !uiState.isSaving,
                    ) {
                        Text(text = if (uiState.isSaving) "保存中..." else "保存")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { onIntent(RecognitionIntent.DismissResult) },
                    ) {
                        Text(text = "取消")
                    }
                },
            )
        }
    }
}
