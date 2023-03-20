package com.example.detector.presentation.ui.graphicOverlay

import android.content.Context
import android.graphics.Canvas

interface Graphic {

    val applicationContext: Context

    fun draw(canvas: Canvas?)

    fun scaleX(horizontal: Float): Float

    fun scaleY(vertical: Float): Float

    fun translateX(x: Float): Float

    fun translateY(y: Float): Float

    fun postInvalidate()

}
