package com.example.detector.presentation.ui.detectorScreen.model

data class DetectorUiCallback(
    val cameraPermissionGranted: (Boolean) -> Unit,
    val camera: () -> Unit,
    val gallery: () -> Unit,
    val clearPhoto: () -> Unit,
)
