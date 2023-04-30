package com.example.detector.presentation.ui.detectorScreen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.Pair
import android.view.View
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.detector.presentation.ui.detectorScreen.model.FaceRecognition
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState.*
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent.RepeatData
import com.example.detector.presentation.viewmodel.DetectorViewModel
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.InterpreterFactory
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.DequantizeOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.roundToInt
import kotlin.math.sqrt


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
        .size(300.dp)

    Box(modifier = modifier.fillMaxSize()) {

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            val painter = data.photoBitmap?.let {
                rememberAsyncImagePainter(it)
            } ?: painterResource(id = R.drawable.test_photo)

            Image(
                painter = painter,
                modifier = modifierForImage
                    .background(colorResource(id = R.color.white))
                    .clickable {
                        onClickChangePhoto.invoke()
                    },
                contentDescription = "",
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

            data.faceBitmap?.let {
                Column(modifier = modifier) {
                    DetectorCanvasFace(
                        modifier = modifier
                            .fillMaxWidth()
                            .size(112.dp),
                        bitmap = it,
                        faces = data.faceList,
                        registered = data.faceRecognition,
                    )
                }
            }
        }
    }

//    if (data.faceList.isNotEmpty()) {
//        Box(modifier = modifier.fillMaxSize()) {
//            DetectorCanvas(
////                modifier = modifierForImage,
//                faces = data.faceList
//            )
//        }
//    }
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
fun DetectorCanvasFace(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    faces: List<Face>,
    registered: ArrayList<FaceRecognition>,
) {

    //Create ByteBuffer to store normalized image
    val context = LocalContext.current
    val inputSize = 112
    val isModelQuantized = false
    val distance = 1f
    val IMAGE_MEAN = 128.0f
    val IMAGE_STD = 128.0f
    val OUTPUT_SIZE = 192 //Output size of model
    var tfLite: Interpreter? = null
    val modelFile = "mobile_face_net.tflite" //model name
//    val modelFile = "mobilenet_quant_v1_224.tflite" //model name
//    val registered = arrayListOf<FaceRecognition>()


    val view = LocalView.current
    val painter = rememberAsyncImagePainter(bitmap)

    val output = Array(1) {
        FloatArray(OUTPUT_SIZE)
    }

    Image(
        painter = painter,
        modifier = modifier.clickable {


            when {
                registered.size == 0 -> {
                    registered.add(FaceRecognition("джей ло", -1f, output))
                }
                registered.size == 1 -> {
                    registered.add(FaceRecognition("я", -1f, output))
                }
                registered.size == 2 -> {
                    registered.add(FaceRecognition("я", -1f, output))
                }
                else -> {}
            }

        },
        contentDescription = "",
    )

    Box(modifier = modifier) {
        val contour = faces.first().allContours
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


    //Load model
    tfLite = loadModelFile(context, modelFile)?.let {
        Interpreter(it)
    }

    val imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
    imgData.order(ByteOrder.nativeOrder())
    val intValues = IntArray(inputSize * inputSize)

//    bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

    imgData.rewind()

    for (i in 0 until inputSize) {
        for (j in 0 until inputSize) {
            val pixelValue = intValues[i * inputSize + j]
            if (isModelQuantized) {
                // Quantized model
                imgData.put((pixelValue shr 16 and 0xFF).toByte())
                imgData.put((pixelValue shr 8 and 0xFF).toByte())
                imgData.put((pixelValue and 0xFF).toByte())
            } else { // Float model
                imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
    }
    val inputArray = arrayOf<Any>(imgData)
    val outputMap: MutableMap<Int, Any> = java.util.HashMap()

    val embeedings = Array(1) { FloatArray(OUTPUT_SIZE) }
//    output of model will be stored in this variable

    outputMap[0] = embeedings     //    outputMap[0] = embeedings

    try {
        tfLite?.let {
            it.runForMultipleInputsOutputs(inputArray, outputMap)
        }
    } catch (e: IOException) {
        Log.e("tfliteSupport", "Error reading model", e)
    }

    var distance_local = Float.MAX_VALUE
    val id = "0"
    val label = "?"

    //Compare new face with saved Faces.

    //Compare new face with saved Faces.
    if (registered.size > 0) {
        val nearest: List<Pair<String, Float>?> = registered.findNearest(embeedings[0])    //TODO
        //Find 2 closest matching face
        if (nearest[0] != null) {
            val name = nearest[0]!!.first //get name and distance of closest matching face
            // label = name;
            distance_local = nearest[0]!!.second
            if (true) {
                if (distance_local < distance) {
                    //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.

                    Text(
                        text = """
                    Nearest: $name
                    Dist: ${String.format("%.3f", distance_local)}
                    2nd Nearest: ${nearest[1]!!.first}
                    Dist: ${String.format("%.3f", nearest[1]!!.second)}
                    """.trimIndent()
                    )

                    if (nearest.size >= 3) {
                        Text(
                            text = """
                    3nd Nearest: ${nearest[2]!!.first}
                    Dist: ${String.format("%.3f", nearest[2]!!.second)}
                    """.trimIndent()
                        )
                    }

                } else {
                    Text(
                        text = """
                    Unknown 
                    Dist: ${String.format("%.3f", distance_local)}
                    Nearest: $name
                    Dist: ${String.format("%.3f", distance_local)}
                    2nd Nearest: ${nearest[1]!!.first}
                    Dist: ${String.format("%.3f", nearest[1]!!.second)}
                    """.trimIndent()
                    )

                    if (nearest.size >= 3) {
                        Text(
                            text = """
                    3nd Nearest: ${nearest[2]!!.first}
                    Dist: ${String.format("%.3f", nearest[2]!!.second)}
                    """.trimIndent()
                        )
                    }
                }

//                    System.out.println("nearest: " + name + " - distance: " + distance_local);
            } else {
                if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    Text(text = name)
                else
                    Text(text = "Unknown")
                //                    System.out.println("nearest: " + name + " - distance: " + distance_local);
            }
        }
    }


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
    return scaleX(x.toFloat())
}

fun translateY(y: Float): Float {
    return scaleY(y.toFloat())
}

fun scaleX(x: Float): Float {
    return x * 1.0f
}

fun scaleY(y: Float): Float {
    return y * 1.0f
}

private fun loadModelFile(activity: Context, MODEL_FILE: String): MappedByteBuffer? {
    return try {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    } catch (exception: Exception) {
        null
    }
}


//    public void register(String name, SimilarityClassifier.Recognition rec) {
//        registered.put(name, rec);
//    }
private fun ArrayList<FaceRecognition>.findNearest(emb: FloatArray): List<Pair<String, Float>?> {
    val neighbour_list: MutableList<Pair<String, Float>?> = ArrayList()
    var ret: Pair<String, Float>? = null //to get closest match
    var prev_ret: Pair<String, Float>? = null //to get second closest match

    for (faceRecognition in this) {
        val knownEmb = FloatArray(192)
        var distances = 0f
        for (i in emb.indices) {
            val diff = emb[i] - knownEmb[i]
            distances += diff * diff
        }
        distances = sqrt(distances.toDouble()).toFloat()
        if (ret == null || distances < ret.second) {
            prev_ret = ret
            ret = Pair(faceRecognition.name, distances)
        }
    }
    if (prev_ret == null)
        prev_ret = ret

    neighbour_list.add(ret)
    neighbour_list.add(prev_ret)

    return neighbour_list
}
