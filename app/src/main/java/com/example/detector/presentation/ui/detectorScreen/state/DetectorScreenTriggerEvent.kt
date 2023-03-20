package com.example.detector.presentation.ui.detectorScreen.state

import android.graphics.Bitmap
import android.net.Uri

sealed class DetectorScreenTriggerEvent {
    object ClearPhoto : DetectorScreenTriggerEvent()
    data class RepeatData(val bitmap: Bitmap) : DetectorScreenTriggerEvent()
    object UploadPhotoFromCamera : DetectorScreenTriggerEvent()
    data class UploadPhotoFromPicker(val uri: Uri) : DetectorScreenTriggerEvent()
}
