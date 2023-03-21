package com.example.detector.presentation.ui.detectorScreen

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.ExperimentalTextApi
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
import com.google.mlkit.vision.face.FaceLandmark

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
    val modifierForImage = modifier
        .fillMaxWidth()
        .height(300.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            val painter = data.photoBitmap?.let {
                rememberAsyncImagePainter(it)
            } ?: painterResource(id = R.drawable.ic_add)


            Image(
                painter = painter,
                modifier = modifierForImage
                    .background(colorResource(id = R.color.white))
                    .clickable {
                        onClickChangePhoto.invoke()
                    },
                contentDescription = "",
//            contentScale = if (data.photoBitmap != null) ContentScale.Crop else
                contentScale = ContentScale.FillWidth
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

//            data.faceList.forEach {
//                it.allContours.map {
//                    Text(text = "${it.points} контур ")
//                }
//            }
        }
    }

    if (data.faceList.isNotEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            DetectorCanvas(
                modifier = modifierForImage,
                faces = data.faceList
            )
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


@OptIn(ExperimentalTextApi::class)
@Composable
fun DetectorCanvas(
    modifier: Modifier = Modifier,
    faces: List<Face>
) {
    val view = LocalView.current

    Column(
    ) {
        Canvas(modifier = modifier) {

//            faces.firstOrNull() { face -> //если надо показать одно лицо
            faces.forEach { face ->
                val instaColors = listOf(Color.Yellow, Color.Red, Color.Magenta)

                // Draws a circle at the position of the detected face, with the face's track id below.
                val x: Float = view.translateX(face.boundingBox.centerX())
                val y: Float = translateY(face.boundingBox.centerY())

                drawCircle(
                    brush = Brush.linearGradient(colors = instaColors),
                    radius = 10f,
                    center = Offset(x, y),
                )

//                drawText(
//                    topLeft = Offset(x + -70.0f, y + 80.0f),
//                    text = "id: " + face.getTrackingId(),
////
////                    x + com.google.codelab.mlkit.FaceContourGraphic.ID_X_OFFSET,
////                    y + com.google.codelab.mlkit.FaceContourGraphic.ID_Y_OFFSET,
////                    idPaint
//
//                )

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
                    topLeft = Offset(x = x, y = y),
                )
//


//                if (face.smilingProbability != null) {
//                    canvas.drawText(
//                        "happiness: " + String.format("%.2f", face.smilingProbability),
//                        x + com.google.codelab.mlkit.FaceContourGraphic.ID_X_OFFSET * 3,
//                        y - com.google.codelab.mlkit.FaceContourGraphic.ID_Y_OFFSET,
//                        idPaint
//                    )
//                }
//
//                if (face.rightEyeOpenProbability != null) {
//                    canvas.drawText(
//                        "right eye: " + String.format("%.2f", face.rightEyeOpenProbability),
//                        x - com.google.codelab.mlkit.FaceContourGraphic.ID_X_OFFSET,
//                        y,
//                        idPaint
//                    )
//                }
//                if (face.leftEyeOpenProbability != null) {
//                    canvas.drawText(
//                        "left eye: " + String.format("%.2f", face.leftEyeOpenProbability),
//                        x + com.google.codelab.mlkit.FaceContourGraphic.ID_X_OFFSET * 6,
//                        y,
//                        idPaint
//                    )
//                }

                val leftEye = face.getLandmark(FaceLandmark.LEFT_EYE)
                if (leftEye != null) {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(leftEye.position.x, leftEye.position.y),
                    )
                }
                val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                if (rightEye != null) {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(rightEye.position.x, rightEye.position.y),
                    )
                }


                val contour = face.allContours
                for (faceContour in contour) {
                    for (point in faceContour.points) {
                        val px: Float = view.translateX(point.x.toInt())
                        val py: Float = translateY(point.y.toInt())
                        drawCircle(
                            brush = Brush.linearGradient(colors = instaColors),
                            radius = 10f,
                            center = Offset(px, py),
                        )
                    }
                }


                val leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK)
                if (leftCheek != null) {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(
                            view.translateX(leftCheek.position.x.toInt()),
                            translateY(leftCheek.position.y.toInt())
                        ),
                    )
                }
                val rightCheek = face.getLandmark(FaceLandmark.RIGHT_CHEEK)
                if (rightCheek != null) {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(
                            view.translateX(rightCheek.position.x.toInt()),
                            translateY(rightCheek.position.y.toInt())
                        ),
                    )
                }

                true
            }
        }
    }
}

fun View.translateX(x: Int): Float {
//    return this.width - scaleX(x.toFloat())  //зеркало
    return scaleX(x.toFloat())
}

fun translateY(y: Int): Float {
    return scaleY(y.toFloat())
}

fun scaleX(x: Float): Float {
    return x * 1.0f
}

fun scaleY(y: Float): Float {
    return y * 1.0f
}
