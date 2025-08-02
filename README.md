# CNIC Scanner App

A smart Android application for scanning and processing Computerized National Identity Cards (CNIC) with automatic detection, cropping, and enhancement features.

## Features

### 🎯 **Automatic CNIC Detection**
- Real-time camera preview with CNIC boundary detection
- ML Kit-powered text recognition for accurate CNIC identification
- Automatic capture when CNIC is properly positioned
- Visual feedback with confidence percentage

### 📷 **Smart Camera Interface**
- Full-screen camera preview
- CNIC-sized rectangle overlay with corner indicators
- Real-time detection status and instructions
- Manual capture button for user control

### ✂️ **Intelligent Image Processing**
- Automatic cropping to CNIC boundaries
- Image enhancement for better readability
- Proper aspect ratio maintenance (1.6:1 for CNIC)
- High-quality JPEG output

### 💾 **Gallery Integration**
- Automatic saving to device gallery
- Organized storage in "CNICScanner" folder
- Compatible with Android 10+ MediaStore API
- File provider support for sharing

### 📱 **User-Friendly Interface**
- Beautiful launcher screen with app introduction
- Intuitive navigation between camera and preview
- Preview screen with save, share, and retake options
- Material Design 3 components

## Technical Implementation

### **Camera Integration**
- Uses CameraX API for modern camera functionality
- ImageAnalysis for real-time CNIC detection
- PreviewView for smooth camera preview
- Proper lifecycle management

### **ML Kit Integration**
- Text recognition for CNIC content detection
- Pattern matching for CNIC numbers and text
- Confidence scoring for detection accuracy
- Fallback to basic edge detection

### **Image Processing**
- YUV to RGB conversion for ML Kit compatibility
- Bitmap manipulation for cropping and enhancement
- Color matrix adjustments for better contrast
- Efficient memory management

### **Storage & Sharing**
- MediaStore API for modern Android versions
- FileProvider for secure file sharing
- Proper permission handling
- Gallery integration

## Permissions Required

- **Camera**: For capturing CNIC images
- **Storage**: For saving processed images to gallery
- **Read Media Images**: For accessing saved images (Android 13+)

## Usage Instructions

1. **Launch the App**: Open CNIC Scanner from your app drawer
2. **Start Scanning**: Tap "Start Scanning" on the launcher screen
3. **Position CNIC**: Hold your CNIC within the rectangular frame
4. **Auto Capture**: The app will automatically capture when CNIC is detected
5. **Manual Capture**: Alternatively, tap "Capture CNIC" button
6. **Preview & Save**: Review the captured image and save to gallery
7. **Share**: Share the processed CNIC image with others

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (API level 24)
- Kotlin 1.8+
- Compose BOM

### Dependencies
```kotlin
// Camera
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// ML Kit
implementation("com.google.mlkit:text-recognition:16.0.0")
implementation("com.google.mlkit:object-detection:17.0.0")

// Image Processing
implementation("androidx.exifinterface:exifinterface:1.3.6")
implementation("io.coil-kt:coil-compose:2.5.0")

// Permissions
implementation("com.google.accompanist:accompanist-permissions:0.32.0")
```

### Build Configuration
```kotlin
android {
    compileSdk = 35
    minSdk = 24
    targetSdk = 35
    
    buildFeatures {
        compose = true
    }
}
```

## Architecture

The app follows a modular architecture with clear separation of concerns:

- **UI Layer**: Compose screens and components
- **Camera Layer**: CameraX integration and image analysis
- **ML Layer**: ML Kit text recognition and CNIC detection
- **Processing Layer**: Image cropping and enhancement
- **Storage Layer**: Gallery saving and file management

## Key Components

### `CameraScreen.kt`
- Main camera interface with preview and detection
- Real-time CNIC detection using ML Kit
- Auto-capture functionality

### `CNICDetector.kt`
- ML Kit integration for text recognition
- CNIC pattern matching and confidence scoring
- Fallback detection mechanisms

### `ImageProcessor.kt`
- Image cropping and enhancement
- Gallery saving with MediaStore API
- File provider configuration

### `PreviewScreen.kt`
- Captured image preview
- Save, share, and retake functionality
- User feedback and status display

## Future Enhancements

- [ ] OCR text extraction from CNIC
- [ ] Multiple document type support
- [ ] Cloud storage integration
- [ ] Batch processing capabilities
- [ ] Advanced image filters
- [ ] Document verification features

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For support and questions, please open an issue on the GitHub repository. 