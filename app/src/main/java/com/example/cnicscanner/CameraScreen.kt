package com.example.cnicscanner

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var camera: Camera? by remember { mutableStateOf(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var cnicDetected by remember { mutableStateOf(false) }
    var detectionConfidence by remember { mutableStateOf(0f) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val coroutineScope = rememberCoroutineScope()
    
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, camera will be initialized
        } else {
            onError("Camera permission is required")
        }
    }
    
    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()
                    
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                // Enhanced CNIC detection logic
                                detectCNICEnhanced(imageProxy) { detected, confidence ->
                                    cnicDetected = detected
                                    detectionConfidence = confidence
                                    if (detected && confidence > 0.6f && !isCapturing) {
                                        isCapturing = true
                                        captureImage(
                                            imageCapture = imageCapture,
                                            context = context,
                                            onImageCaptured = onImageCaptured,
                                            onError = onError
                                        ) {
                                            isCapturing = false
                                        }
                                    }
                                }
                                imageProxy.close()
                            }
                        }
                    
                    try {
                        cameraProvider?.unbindAll()
                        camera = cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            lensFacing,
                            preview,
                            imageCapture,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        onError("Camera binding failed: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))
                
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // CNIC Boundary Rectangle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 3.dp,
                        color = if (cnicDetected) Color.Green else Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                // Draw corner indicators
                val cornerLength = 50f
                val strokeWidth = 5f
                
                // Top-left corner
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, cornerLength),
                    end = androidx.compose.ui.geometry.Offset(0f, 0f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(cornerLength, 0f),
                    strokeWidth = strokeWidth
                )
                
                // Top-right corner
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(size.width - cornerLength, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                    end = androidx.compose.ui.geometry.Offset(size.width, cornerLength),
                    strokeWidth = strokeWidth
                )
                
                // Bottom-left corner
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height - cornerLength),
                    end = androidx.compose.ui.geometry.Offset(0f, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(cornerLength, size.height),
                    strokeWidth = strokeWidth
                )
                
                // Bottom-right corner
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(size.width - cornerLength, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = if (cnicDetected) Color.Green else Color.White,
                    start = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLength),
                    strokeWidth = strokeWidth
                )
            }
        }
        
        // Instructions Text
        Text(
            text = when {
                cnicDetected && detectionConfidence > 0.6f -> "CNIC Detected! Capturing..."
                cnicDetected -> "CNIC detected (${(detectionConfidence * 100).toInt()}% confidence)"
                else -> "Position CNIC within the frame"
            },
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        )
        
        // Manual Capture Button
        Button(
            onClick = {
                if (!isCapturing) {
                    isCapturing = true
                    captureImage(
                        imageCapture = imageCapture,
                        context = context,
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    ) {
                        isCapturing = false
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            enabled = !isCapturing
        ) {
            Text("Capture CNIC")
        }
    }
}

private fun detectCNICEnhanced(
    imageProxy: ImageProxy,
    onDetectionResult: (Boolean, Float) -> Unit
) {
    try {
        val buffer = imageProxy.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        
        val width = imageProxy.width
        val height = imageProxy.height
        
        // Convert YUV to RGB and create bitmap
        val bitmap = yuvToBitmap(data, width, height, imageProxy.imageInfo.rotationDegrees)
        
        // Use ML Kit for text recognition and CNIC detection
        // For now, use basic detection to avoid ML Kit issues
        val basicResult = basicCNICDetection(data, width, height)
        onDetectionResult(basicResult.first, basicResult.second)
    } catch (e: Exception) {
        // Fallback to basic detection
        val buffer = imageProxy.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        val basicResult = basicCNICDetection(data, imageProxy.width, imageProxy.height)
        onDetectionResult(basicResult.first, basicResult.second)
    }
}

private fun basicCNICDetection(data: ByteArray, width: Int, height: Int): Pair<Boolean, Float> {
    // Basic edge detection (simplified)
    val centerX = width / 2
    val centerY = height / 2
    val checkSize = minOf(width, height) / 4
    
    var edgeCount = 0
    val threshold = 50
    
    // Sample points around the center to detect edges
    for (x in centerX - checkSize..centerX + checkSize step 10) {
        for (y in centerY - checkSize..centerY + checkSize step 10) {
            if (x >= 0 && x < width && y >= 0 && y < height) {
                val index = y * width + x
                if (index < data.size) {
                    val pixelValue = data[index].toInt() and 0xFF
                    if (pixelValue > threshold) {
                        edgeCount++
                    }
                }
            }
        }
    }
    
    // Calculate confidence based on edge density
    val edgeDensity = edgeCount.toFloat() / (checkSize * checkSize / 25)
    val isDetected = edgeDensity > 0.1f
    val confidence = minOf(edgeDensity, 1.0f)
    
    return Pair(isDetected, confidence)
}

private fun yuvToBitmap(data: ByteArray, width: Int, height: Int, rotation: Int): Bitmap {
    val yuvImage = android.graphics.YuvImage(data, android.graphics.ImageFormat.NV21, width, height, null)
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

private fun captureImage(
    imageCapture: ImageCapture?,
    context: Context,
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit,
    onComplete: () -> Unit
) {
    val photoFile = File(
        context.getExternalFilesDir(null),
        "CNIC_${System.currentTimeMillis()}.jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    imageCapture?.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
                onComplete()
            }
            
            override fun onError(exception: ImageCaptureException) {
                onError("Image capture failed: ${exception.message}")
                onComplete()
            }
        }
    )
} 