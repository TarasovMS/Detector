package com.example.detector.presentation.ui.detectorScreen

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.detector.common.EMPTY_STRING
import com.example.detector.presentation.ui.ErrorOccurredScreen
import com.example.detector.presentation.ui.ProgressIndicatorInCenter
import com.example.detector.presentation.ui.detectorScreen.model.DetectorBitmaps
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
import com.example.detector.presentation.ui.detectorScreen.model.FaceRecognition
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState.*
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent.RepeatData
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
        },
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
    Box(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            val painter = data.detectorBitmaps.photoBitmap?.let {
                rememberAsyncImagePainter(it)
            } ?: painterResource(id = R.drawable.test_photo)

            Image(
                painter = painter,
                modifier = modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .size(300.dp)
                    .background(colorResource(id = R.color.white))
                    .clickable {
                        onClickChangePhoto.invoke()
                    },
                contentDescription = EMPTY_STRING,
                contentScale = ContentScale.FillWidth
            )

            Button(
                modifier = modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    data.detectorBitmaps.photoBitmap?.let {
                        repeatOperationButton.invoke(it)
                    }
                },
            ) {
                Text(text = stringResource(R.string.ok))
            }

            data.detectorBitmaps.faceBitmap?.let {
                Column(modifier = modifier) {
                    DetectorCanvasFace(
                        modifier = modifier
                            .fillMaxWidth()
                            .size(112.dp),
                        bitmap = it,
                        data = data,
                    )
                }
            }
        }
    }
}

@Composable
fun DetectorCanvasFace(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    data: DetectorUiData,
) {
    val view = LocalView.current
    val painter = rememberAsyncImagePainter(bitmap)

    Image(
        modifier = modifier,
        painter = painter,
        contentDescription = "",
    )

    Box(modifier = modifier) {
        val contour = data.faceData.faceList.first().allContours
        for (faceContour in contour) {
            for (point in faceContour.points) {
                val pointX: Float = view.translateX(point.x)
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

    Text(text = data.nearestName.name)

    TextButton(
        onClick = { data.mainData.showDialog = true }
    ) {
        Text(text = "Добавить лицо")
    }

    if (data.mainData.showDialog)
        AddNameAlertDialog(
            data = data.mainData,
            onClickAdd = {
                with(data.faceData) {
                    faceRecognition.add(
                        FaceRecognition(
                            data.mainData.name,
                            -1f,
                            data.embeedingsData.embeedings
                        )
                    )
                }
            }
        )
}

@Composable
fun DetectorCanvas(
    modifier: Modifier = Modifier.fillMaxSize(),
    faces: List<Face>
) {
    val view = LocalView.current

    Column(
    ) {
        Canvas(modifier = modifier.width(100.dp)) {

//            faces.firstOrNull() { face -> //если надо показать одно лицо
            faces.forEach { face ->
                val instaColors = listOf(Color.Yellow, Color.Red, Color.Magenta)

                // Draws a circle at the position of the detected face, with the face's track id below.
                val x: Float = view.translateX(face.boundingBox.centerX().toFloat())
                val y: Float = translateY(face.boundingBox.centerY().toFloat())

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
//                    topLeft = Offset(x = x, y = y),
                    topLeft = Offset(x = left, y = top),
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
                leftEye?.let {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(leftEye.position.x, leftEye.position.y),
                    )
                }
                val rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE)
                rightEye?.let {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(rightEye.position.x, rightEye.position.y),
                    )
                }


                val contour = face.allContours
                for (faceContour in contour) {
                    for (point in faceContour.points) {
                        val pointX: Float = view.translateX(point.x)
                        val pointY: Float = translateY(point.y)

                        drawCircle(
                            brush = Brush.linearGradient(colors = instaColors),
                            radius = 10f,
                            center = Offset(pointX, pointY),
                        )
                    }
                }


                val leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK)
                leftCheek?.let {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(
                            view.translateX(leftCheek.position.x),
                            translateY(leftCheek.position.y)
                        ),
                    )
                }

                val rightCheek = face.getLandmark(FaceLandmark.RIGHT_CHEEK)
                rightCheek?.let {
                    drawCircle(
                        brush = Brush.linearGradient(colors = instaColors),
                        radius = 10f,
                        center = Offset(
                            view.translateX(rightCheek.position.x),
                            translateY(rightCheek.position.y)
                        ),
                    )
                }

                false
            }
        }
    }
}

fun View.translateX(x: Float): Float {
//    return this.width - scaleX(x.toFloat())  //зеркально отображение
    return scaleX(x)
}

fun translateY(y: Float): Float {
    return scaleY(y)
}

fun scaleX(x: Float): Float {
    return x * 1.0f
}

fun scaleY(y: Float): Float {
    return y * 1.0f
}

@Preview(showBackground = true)
@Composable
fun DetectorScreenContentPreview() {
    DetectorScreenContent(
        data = DetectorUiData(
            detectorBitmaps = DetectorBitmaps(
                photoBitmapInit = null,
                faceBitmapInit = null,
                uriNewPhotoInit = Uri.EMPTY
            ),
        ),
        uiTrigger = {},
        repeatOperationButton = {},
        onClickChangePhoto = {},
    )
}
