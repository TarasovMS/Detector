package com.example.detector.presentation.ui.detectorScreen.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.detector.common.DISTANCE_DEFAULT
import com.example.detector.common.EMPTY_STRING
import com.google.mlkit.vision.face.Face

data class DetectorUiData(
    val detectorBitmaps: DetectorBitmaps = DetectorBitmaps(),
    val embeedingsData: Embeedings = Embeedings(),
    val faceData: FaceData = FaceData(),
    val nearestName: NearestName = NearestName(),
    val mainData: MainData = MainData(),
)

class MainData(
    nameInit: String = EMPTY_STRING,
    showDialogInit: Boolean = false,
) {
    var name: String by mutableStateOf(nameInit)
    var showDialog: Boolean by mutableStateOf(showDialogInit)
}

class NearestName(
    nameInit: String = EMPTY_STRING,
) {
    var name: String by mutableStateOf(nameInit)
}

class Embeedings(
    embeedingsInit: Array<FloatArray> = Array(1) { FloatArray(192) },
) {
    var embeedings: Array<FloatArray> by mutableStateOf(embeedingsInit)
}

class FaceData(
    faceListInit: List<Face> = emptyList(),
    faceRecognitionInit: ArrayList<FaceRecognition> = arrayListOf(),
) {
    var faceList: List<Face> by mutableStateOf(faceListInit)
    var faceRecognition: ArrayList<FaceRecognition> by mutableStateOf(faceRecognitionInit)
}

class DetectorBitmaps(
    photoBitmapInit: Bitmap? = null,
    faceBitmapInit: Bitmap? = null,
    uriNewPhotoInit: Uri = Uri.EMPTY,
) {
    var photoBitmap: Bitmap? by mutableStateOf(photoBitmapInit)
    var faceBitmap: Bitmap? by mutableStateOf(faceBitmapInit)
    var uriNewPhoto: Uri by mutableStateOf(uriNewPhotoInit)
}

class FaceRecognition(
    nameInit: String = EMPTY_STRING,
    distanceInit: Float = DISTANCE_DEFAULT,
    outputInit: Array<FloatArray> = arrayOf(),
) {
    var name: String by mutableStateOf(nameInit)
    var distance: Float by mutableStateOf(distanceInit)
    var output: Array<FloatArray> by mutableStateOf(outputInit)
}
