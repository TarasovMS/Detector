package com.example.detector.presentation.ui.detectorScreen.state

import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData

sealed class DetectorScreenState {
    object DetectorScreenProgress : DetectorScreenState()
    data class DetectorScreenError(val error: Error) : DetectorScreenState()
    data class DetectorScreenLoadComplete(val data: DetectorUiData) : DetectorScreenState()
}
