package com.example.detector.presentation.ui.graphicOverlay

import android.hardware.camera2.CameraCharacteristics

class GraphicOverlay(

) {
    private val lock = Any()
    private val previewWidth = 0
    private val widthScaleFactor = 1.0f
    private val previewHeight = 0
    private val heightScaleFactor = 1.0f
    private val facing = CameraCharacteristics.LENS_FACING_BACK
    private var graphics: Set<Graphic> = HashSet()

    fun clear() {
        synchronized(lock) {
            graphics = HashSet()
        }
//        postInvalidate()
    }

    fun add(graphic: Graphic?) {
        synchronized(lock) {
            graphics.plus(graphic)
        }
//        postInvalidate()
    }


}

