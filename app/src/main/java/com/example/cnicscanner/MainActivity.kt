package com.example.cnicscanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.cnicscanner.ui.theme.CNICScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CNICScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CNICScannerApp()
                }
            }
        }
    }
}

@Composable
fun CNICScannerApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Launcher) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    
    when (currentScreen) {
        Screen.Launcher -> {
            LauncherScreen(
                onStartScanning = {
                    currentScreen = Screen.Camera
                }
            )
        }
        Screen.Camera -> {
            CameraScreen(
                onImageCaptured = { uri ->
                    capturedImageUri = uri
                    currentScreen = Screen.Preview
                },
                onError = { error ->
                    // Show error message
                }
            )
        }
        Screen.Preview -> {
            capturedImageUri?.let { uri ->
                PreviewScreen(
                    imageUri = uri,
                    onSaveToGallery = {
                        // Image already saved in PreviewScreen
                    },
                    onShare = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, uri)
                            type = "image/jpeg"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share CNIC"))
                    },
                    onRetake = {
                        currentScreen = Screen.Camera
                    },
                    onBack = {
                        currentScreen = Screen.Camera
                    }
                )
            }
        }
    }
}

sealed class Screen {
    object Launcher : Screen()
    object Camera : Screen()
    object Preview : Screen()
}