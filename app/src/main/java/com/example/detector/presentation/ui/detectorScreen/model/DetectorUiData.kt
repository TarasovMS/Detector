package com.example.detector.presentation.ui.detectorScreen.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.vision.face.Face

class DetectorUiData(
    photoBitmapInit: Bitmap? = null,
    faceBitmapInit: Bitmap? = null,
    uriNewPhotoInit: Uri = Uri.EMPTY,
    faceListInit: List<Face> = emptyList(),
) {
    val photoBitmap: Bitmap? by mutableStateOf(photoBitmapInit)
    val faceBitmap: Bitmap? by mutableStateOf(faceBitmapInit)
    val uriNewPhoto: Uri by mutableStateOf(uriNewPhotoInit)
    val faceList: List<Face> by mutableStateOf(faceListInit)
}
