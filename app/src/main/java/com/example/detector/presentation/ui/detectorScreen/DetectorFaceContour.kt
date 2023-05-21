package com.example.detector.presentation.ui.detectorScreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.detector.common.translateX
import com.example.detector.common.translateY
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData

@Composable
fun DetectorFaceContour(
    modifier: Modifier = Modifier,
    data: DetectorUiData,
) {
    val contour = data.faceData.faceList.first().allContours

    Box(
        modifier = modifier.size(200.dp)
    ) {
        for (faceContour in contour) {
            for (point in faceContour.points) {
                val pointX: Float = translateX(point.x)
                val pointY: Float = translateY(point.y)

                val instagramColors = listOf(Color.Yellow, Color.Red, Color.Magenta)

                Canvas(modifier = modifier) {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instagramColors),
                        radius = 10f,
                        center = Offset(pointX, pointY),
                    )
                }
            }
        }
    }
}
