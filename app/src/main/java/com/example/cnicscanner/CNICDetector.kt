package com.example.cnicscanner

import android.graphics.Bitmap
import android.graphics.Rect
// ML Kit imports commented out to avoid compilation issues
// import com.google.mlkit.vision.common.InputImage
// import com.google.mlkit.vision.text.TextRecognition
// import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CNICDetector {
    
    // ML Kit text recognizer commented out to avoid compilation issues
    // private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    suspend fun detectCNICInImage(bitmap: Bitmap): CNICDetectionResult {
        // Simplified detection without ML Kit for now
        return analyzeTextForCNIC("", bitmap)
    }
    
    private fun analyzeTextForCNIC(text: String, bitmap: Bitmap): CNICDetectionResult {
        // CNIC typically contains specific patterns
        val cnicPatterns = listOf(
            "CNIC", "NIC", "Identity Card", "National Identity",
            "Pakistan", "PAK", "NADRA", "Identity Number"
        )
        
        val hasCNICText = cnicPatterns.any { pattern ->
            text.contains(pattern, ignoreCase = true)
        }
        
        // Check for CNIC number pattern (13 digits)
        val cnicNumberPattern = Regex("\\d{5}-\\d{7}-\\d")
        val hasCNICNumber = cnicNumberPattern.containsMatchIn(text)
        
        // Check for rectangular shape
        val isRectangular = detectRectangularShape(bitmap)
        
        val isCNIC = hasCNICText || hasCNICNumber || isRectangular
        
        val bounds = if (isCNIC) {
            // Estimate CNIC bounds based on text blocks
            estimateCNICBounds(bitmap)
        } else null
        
        return CNICDetectionResult(
            isDetected = isCNIC,
            bounds = bounds,
            confidence = calculateConfidence(hasCNICText, hasCNICNumber, isRectangular)
        )
    }
    
    private fun detectRectangularShape(bitmap: Bitmap): Boolean {
        // Simple edge detection for rectangular shape
        val width = bitmap.width
        val height = bitmap.height
        
        // Check if the image has a rectangular aspect ratio (typical for CNIC)
        val aspectRatio = width.toFloat() / height.toFloat()
        val isRectangularAspect = aspectRatio in 1.4f..1.8f // CNIC aspect ratio range
        
        // Check for strong edges at the boundaries
        val edgeStrength = calculateEdgeStrength(bitmap)
        
        return isRectangularAspect && edgeStrength > 0.3f
    }
    
    private fun calculateEdgeStrength(bitmap: Bitmap): Float {
        val width = bitmap.width
        val height = bitmap.height
        var edgePixels = 0
        val totalPixels = width * height
        
        // Sample pixels to detect edges
        for (x in 0 until width step 5) {
            for (y in 0 until height step 5) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (android.graphics.Color.red(pixel) + 
                           android.graphics.Color.green(pixel) + 
                           android.graphics.Color.blue(pixel)) / 3
                
                if (gray < 128) { // Dark pixel (potential edge)
                    edgePixels++
                }
            }
        }
        
        return edgePixels.toFloat() / totalPixels
    }
    
    private fun estimateCNICBounds(bitmap: Bitmap): Rect {
        val width = bitmap.width
        val height = bitmap.height
        
        // CNIC typically takes up 70-90% of the image
        val cnicWidth = (width * 0.8).toInt()
        val cnicHeight = (cnicWidth / 1.6).toInt() // CNIC aspect ratio
        
        val left = (width - cnicWidth) / 2
        val top = (height - cnicHeight) / 2
        
        return Rect(left, top, left + cnicWidth, top + cnicHeight)
    }
    
    private fun calculateConfidence(
        hasCNICText: Boolean,
        hasCNICNumber: Boolean,
        isRectangular: Boolean
    ): Float {
        var confidence = 0f
        if (hasCNICText) confidence += 0.4f
        if (hasCNICNumber) confidence += 0.4f
        if (isRectangular) confidence += 0.2f
        return confidence
    }
}

data class CNICDetectionResult(
    val isDetected: Boolean,
    val bounds: Rect?,
    val confidence: Float,
    val errorMessage: String? = null
) 