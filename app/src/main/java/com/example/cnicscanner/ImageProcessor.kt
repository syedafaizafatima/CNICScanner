package com.example.cnicscanner

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageProcessor {
    
    fun cropAndEnhanceCNIC(
        context: Context,
        originalUri: Uri,
        onSuccess: (Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val originalBitmap = getBitmapFromUri(context, originalUri)
            if (originalBitmap == null) {
                onError("Failed to load original image")
                return
            }
            
            // Detect CNIC boundaries (simplified - in real app use ML Kit)
            val cnicBounds = detectCNICBounds(originalBitmap)
            
            // Crop the CNIC
            val croppedBitmap = cropBitmap(originalBitmap, cnicBounds)
            
            // Enhance the image
            val enhancedBitmap = enhanceImage(croppedBitmap)
            
            // Save to gallery
            val savedUri = saveToGallery(context, enhancedBitmap)
            onSuccess(savedUri)
            
        } catch (e: Exception) {
            onError("Image processing failed: ${e.message}")
        }
    }
    
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun detectCNICBounds(bitmap: Bitmap): Rect {
        // Simplified CNIC detection - in a real app, use ML Kit Object Detection
        // For now, we'll use the center portion of the image
        val width = bitmap.width
        val height = bitmap.height
        
        // CNIC typically has a 1.6:1 aspect ratio
        val cnicWidth = (width * 0.8).toInt()
        val cnicHeight = (cnicWidth / 1.6).toInt()
        
        val left = (width - cnicWidth) / 2
        val top = (height - cnicHeight) / 2
        
        return Rect(left, top, left + cnicWidth, top + cnicHeight)
    }
    
    private fun cropBitmap(bitmap: Bitmap, bounds: Rect): Bitmap {
        return try {
            Bitmap.createBitmap(bitmap, bounds.left, bounds.top, bounds.width(), bounds.height())
        } catch (e: Exception) {
            bitmap // Return original if cropping fails
        }
    }
    
    private fun enhanceImage(bitmap: Bitmap): Bitmap {
        val enhancedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(enhancedBitmap)
        
        // Apply contrast and brightness adjustments
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                setSaturation(1.2f) // Increase saturation
            })
        }
        
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        
        return enhancedBitmap
    }
    
    private fun saveToGallery(context: Context, bitmap: Bitmap): Uri {
        val filename = "CNIC_${System.currentTimeMillis()}.jpg"
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, use MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CNICScanner")
            }
            
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { savedUri ->
                context.contentResolver.openOutputStream(savedUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
                savedUri
            } ?: throw IOException("Failed to create new MediaStore record.")
        } else {
            // For older Android versions, save to external storage
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val cnicDir = File(imagesDir, "CNICScanner")
            if (!cnicDir.exists()) {
                cnicDir.mkdirs()
            }
            
            val imageFile = File(cnicDir, filename)
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }
            
            // Convert to content URI for sharing
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        }
    }
    
    fun getFileProviderAuthority(context: Context): String {
        return "${context.packageName}.fileprovider"
    }
} 