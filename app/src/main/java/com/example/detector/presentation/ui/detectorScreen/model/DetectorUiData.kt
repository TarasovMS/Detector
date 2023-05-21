package com.example.detector.presentation.ui.detectorScreen.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.detector.common.EMPTY_STRING
import com.google.mlkit.vision.face.Face

data class DetectorUiData(
    val detectorBitmaps: DetectorBitmaps = DetectorBitmaps(),
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

class FaceData(
    faceListInit: List<Face> = emptyList(),
    faceRecognitionInit: ArrayList<FaceRecognition> = arrayListOf(),
    faceModelDataInit: Array<FloatArray> = Array(1) { FloatArray(192) },
) {
    var faceList: List<Face> by mutableStateOf(faceListInit)
    var faceRecognition: ArrayList<FaceRecognition> by mutableStateOf(faceRecognitionInit)
    var faceModelData: Array<FloatArray> by mutableStateOf(faceModelDataInit)
}

data class DetectorBitmaps(
    val photoBitmap: Bitmap? = null,
    val faceBitmap: Bitmap? = null,
    val uriNewPhoto: Uri = Uri.EMPTY,
)

class FaceRecognition(
    nameInit: String = EMPTY_STRING,
    outputInit: Array<FloatArray> = arrayOf(),
) {
    var name: String by mutableStateOf(nameInit)
    var output: Array<FloatArray> by mutableStateOf(outputInit)
}
