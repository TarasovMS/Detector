package com.example.detector.presentation.ui.detectorScreen

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.detector.R
import com.example.detector.presentation.ui.ErrorOccurredScreen
import com.example.detector.presentation.ui.ProgressIndicatorInCenter
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState.*
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent.*
import com.example.detector.presentation.viewmodel.DetectorViewModel
import com.google.mlkit.vision.face.Face

@Composable
fun DetectorScreen(viewModel: DetectorViewModel = hiltViewModel()) {
    val uiState by viewModel.detectorUiStateFlow.collectAsState()

    DetectorScreenHandler(
        uiState = uiState,
        uiTrigger = {
            viewModel.onTriggerEvent(it)
        },
        repeatOperationButton = {
            viewModel.onTriggerEvent(RepeatData(it))
        }
    )
}

@Composable
fun DetectorScreenHandler(
    uiState: DetectorScreenState,
    uiTrigger: (DetectorScreenTriggerEvent) -> Unit,
    repeatOperationButton: (Bitmap) -> Unit,
) {
    when (uiState) {
        is DetectorScreenError -> {
            ErrorOccurredScreen(error = uiState.error) {
//                repeatOperationButton.invoke()
            }
        }
        is DetectorScreenLoadComplete -> {
            DetectorScreenBottomSheet(
                data = uiState.data,
                uiTrigger = uiTrigger,
                repeatOperationButton = repeatOperationButton,
            )
        }
        DetectorScreenProgress -> {
            ProgressIndicatorInCenter()
        }
    }
}

@Composable
fun DetectorScreenContent(
    modifier: Modifier = Modifier,
    data: DetectorUiData,
    uiTrigger: (DetectorScreenTriggerEvent) -> Unit,
    repeatOperationButton: (Bitmap) -> Unit,
    onClickChangePhoto: () -> Unit,
) {

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val painter = data.photoBitmap?.let {
            rememberAsyncImagePainter(it)
        } ?: painterResource(id = R.drawable.ic_add)

        Image(
            painter = painter,
            modifier = modifier
                .height(300.dp)
                .width(200.dp)
                .background(colorResource(id = R.color.white))
                .clickable {
                    onClickChangePhoto.invoke()
                },
            contentDescription = "",
//            contentScale = if (data.photoBitmap != null) ContentScale.Crop else ContentScale.Fit
        )

        Button(
            modifier = modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            onClick = {
                data.photoBitmap?.let {
                    repeatOperationButton.invoke(it)
                }
            },
        ) {
            Text(text = stringResource(R.string.ok))
        }

        data.faceList.forEach {
            it.allContours.map {
                Text(text = "${it.points} контур ")
            }
        }


        if (data.faceList.isNotEmpty()) {
            DetectorCanvas(faces = data.faceList)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetectorScreenContentPreview() {
    DetectorScreenContent(
        data = DetectorUiData(
            photoBitmapInit = null,
            uriNewPhotoInit = Uri.EMPTY,
        ),
        uiTrigger = {},
        repeatOperationButton = {},
        onClickChangePhoto = {},
    )
}


@Composable
fun DetectorCanvas(faces: List<Face>) {
    val view = LocalView.current

    Canvas(modifier = Modifier) {

        faces.firstOrNull() { face ->
            val instaColors = listOf(Color.Yellow, Color.Red, Color.Magenta)

            // Draws a circle at the position of the detected face, with the face's track id below.
            val x: Float = view.translateX(face.boundingBox.centerX())
            val y: Float = view.translateY(face.boundingBox.centerY())

//        canvas.drawCircle(
//            x,
//            y,
//            com.google.codelab.mlkit.FaceContourGraphic.FACE_POSITION_RADIUS,
//            facePositionPaint
//        )
//        canvas.drawText(
//            "id: " + face.getTrackingId(),
//            x + com.google.codelab.mlkit.FaceContourGraphic.ID_X_OFFSET,
//            y + com.google.codelab.mlkit.FaceContourGraphic.ID_Y_OFFSET,
//            idPaint
//        )

            // Draws a bounding box around the face.

            // Draws a bounding box around the face.
            val xOffset: Float = scaleX(face.boundingBox.width() / 2.0f)
            val yOffset: Float = scaleY(face.boundingBox.height() / 2.0f)
            val left = x - xOffset
            val top = y - yOffset
            val right = x + xOffset
            val bottom = y + yOffset

//            canvas.drawRect(left, top, right, bottom, boxPaint)

            drawRoundRect(
                brush = Brush.linearGradient(colors = instaColors),
                cornerRadius = CornerRadius(60f, 60f),
                style = Stroke(width = 15f, cap = StrokeCap.Round),
                topLeft = Offset(x = left, y = top),
            )

            drawCircle(
                brush = Brush.linearGradient(colors = instaColors),
                radius = 45f,
                style = Stroke(width = 15f, cap = StrokeCap.Round)
            )
            drawCircle(
                brush = Brush.linearGradient(colors = instaColors),
                radius = 13f,
                center = Offset(this.size.width * .80f, this.size.height * 0.20f),
            )

            true
        }
    }
}

fun View.translateX(x: Int): Float {
    return this.width - scaleX(x.toFloat())
}

fun View.translateY(y: Int): Float {
    return scaleY(y.toFloat())
}

fun scaleX(x: Float): Float {
    return x * 1.0f
}

fun scaleY(y: Float): Float {
    return y * 1.0f
}
