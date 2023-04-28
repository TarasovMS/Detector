package com.example.detector.presentation.ui.detectorScreen.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.detector.common.EMPTY_STRING
import com.google.mlkit.vision.face.Face

class DetectorUiData(
    photoBitmapInit: Bitmap? = null,
    faceBitmapInit: Bitmap? = null,
    uriNewPhotoInit: Uri = Uri.EMPTY,
    faceListInit: List<Face> = emptyList(),
    faceRecognitionInit: ArrayList<FaceRecognition> = arrayListOf(),
) {
    var photoBitmap: Bitmap? by mutableStateOf(photoBitmapInit)
    var faceBitmap: Bitmap? by mutableStateOf(faceBitmapInit)
    var uriNewPhoto: Uri by mutableStateOf(uriNewPhotoInit)
    var faceList: List<Face> by mutableStateOf(faceListInit)
    var faceRecognition: ArrayList<FaceRecognition> by mutableStateOf(faceRecognitionInit)
}

class FaceRecognition(
    nameInit: String = EMPTY_STRING,
    distanceInit: Float = 0f,
    outputInit: Array<FloatArray> = arrayOf(),
) {
    var name: String by mutableStateOf(nameInit)
    var distance: Float by mutableStateOf(distanceInit)
    var output: Array<FloatArray> by mutableStateOf(outputInit)
}
