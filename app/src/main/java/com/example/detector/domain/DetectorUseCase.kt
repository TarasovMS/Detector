package com.example.detector.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import android.util.Pair
import arrow.core.Either
import arrow.core.right
import com.example.detector.common.contextProvider.ResourceProviderContext
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
import com.example.detector.presentation.ui.detectorScreen.model.FaceRecognition
import com.google.mlkit.vision.face.Face
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import kotlin.math.sqrt

class DetectorUseCase @Inject constructor(
    private val detectorRepository: DetectorRepository,
    private val contextProvider: ResourceProviderContext,
) {

    fun createTempFilesForPhotos() {
        detectorRepository.createTempFilesForPhotos()
    }

    suspend fun getUriForPhoto(): Either<Nothing, Uri> {
        return detectorRepository.getUriForPhoto()
    }

    suspend fun getImageFromGalleryAndPost(uri: Uri): Either<Error, Bitmap> {
        return detectorRepository.getImageFromGalleryAndPost(uri = uri)
    }

    suspend fun addImageToGalleryAndPost(): Either<Error, Bitmap> {
        return detectorRepository.addImageToGalleryAndPost()
    }

    fun handleDetector(faces: List<Face>, bitmap: Bitmap): Either<Error, Bitmap?> {
        return (if (faces.isNotEmpty()) {
            val face: Face = faces.first()
            val boundingBox = RectF(face.boundingBox)

            val faceBitmap = Bitmap.createBitmap(
                bitmap,
                boundingBox.left.toInt(),
                boundingBox.top.toInt(),
                face.boundingBox.width(),
                face.boundingBox.height(),
            )

            Bitmap.createScaledBitmap(faceBitmap, INPUT_SIZE, INPUT_SIZE, true)
        } else {
            null
        }).right()
    }

    fun recognizeImage(bitmap: Bitmap, data: DetectorUiData): Either<Error, String> {
        val isModelQuantized = false
        val IMAGE_MEAN = 128.0f
        val IMAGE_STD = 128.0f
        val OUTPUT_SIZE = 192
        val distance = 1.0f

        //Load model
        val tfLite = contextProvider.getContext().loadModelFile()?.let {
            Interpreter(it)
        }

        val imgData = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())

        //Делаю копию битмапа для получения доступа к пикселю
        val bitmapCopy: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        val intValues = IntArray(bitmapCopy.width * bitmapCopy.height)
        bitmapCopy.getPixels(
            intValues,
            0,
            bitmapCopy.width,
            0,
            0,
            bitmapCopy.width,
            bitmapCopy.height
        )

        imgData.rewind()

        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val pixelValue = intValues[i * INPUT_SIZE + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData.put((pixelValue and 0xFF).toByte())
                } else {
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }
        val inputArray = arrayOf<Any>(imgData)
        val outputMap: MutableMap<Int, Any> = java.util.HashMap()

//    output of model will be stored in this variable

        data.embeedingsData.embeedings = Array(1) { FloatArray(OUTPUT_SIZE) }

        outputMap[0] = data.embeedingsData.embeedings

        try {
            tfLite?.let {
                it.runForMultipleInputsOutputs(inputArray, outputMap)
            }
        } catch (e: IOException) {
            Log.e("tfliteSupport", "Error reading model", e)
        }

        //Compare new face with saved Faces.
        return if (data.faceData.faceRecognition.size > 0) {
            val nearest: List<Pair<String, Float>?> =
                data.faceData.faceRecognition.findNearest(data.embeedingsData.embeedings.first())    //TODO
            //Find 2 closest matching face

            if (nearest[0] != null) {
                val name =
                    nearest[0]!!.first //get name and distance of closest matching face
                // label = name;
                val distance_local = nearest[0]!!.second
                if (nearest[0] != null) {
                    if (distance_local < distance) {
                        """
                    Nearest: $name
                    Dist: ${String.format("%.3f", distance_local)}
                    2nd Nearest: ${nearest[1]!!.first}
                    Dist: ${String.format("%.3f", nearest[1]!!.second)}
                    
                    ${
                            if (nearest.size > 2) {
                                """
                                    3nd Nearest: ${nearest[2]!!.first}
                                Dist: ${String.format("%.3f", nearest[2]!!.second)}
                                  """.trimIndent()
                            } else {
                                ""
                            }
                        }
                    
                    """.trimIndent()
                    } else {
                        """
                    Unknown 
                    Dist: ${String.format("%.3f", distance_local)}
                    Nearest: $name
                    Dist: ${String.format("%.3f", distance_local)}
                    2nd Nearest: ${nearest[1]!!.first}
                    Dist: ${String.format("%.3f", nearest[1]!!.second)}
                   
                    ${
                            if (nearest.size > 2) {
                                """
                                    3nd Nearest: ${nearest[2]!!.first}
                                Dist: ${String.format("%.3f", nearest[2]!!.second)}
                                  """.trimIndent()
                            } else {
                                ""
                            }
                        }
                    """.trimIndent()
                    }

                } else {
                    if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                        name
                    else
                        "Unknown"
                }
            } else ""

        } else {
            ""
        }.right()
    }

    private fun Context.loadModelFile(): MappedByteBuffer? {
        return try {
            val fileDescriptor = this.assets.openFd(MODEL_FILE)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (exception: Exception) {
            null
        }
    }

    private fun ArrayList<FaceRecognition>.findNearest(emb: FloatArray): List<Pair<String, Float>?> {
        val neighbourList: MutableList<Pair<String, Float>?> = ArrayList()
        var ret: Pair<String, Float>? = null //to get closest match
        var prevRet: Pair<String, Float>? = null //to get second closest match

        for (faceRecognition in this) {
            val knownEmb = faceRecognition.output[0]
            var distances = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distances += diff * diff
            }
            distances = sqrt(distances.toDouble()).toFloat()
            if (ret == null || distances < ret!!.second) {
                prevRet = ret
                ret = Pair(faceRecognition.name, distances)
            }
        }
        if (prevRet == null)
            prevRet = ret

        neighbourList.add(ret)
        neighbourList.add(prevRet)

        return neighbourList
    }

    private companion object {
        private const val MODEL_FILE = "mobile_face_net.tflite"
        private const val INPUT_SIZE = 112
    }
}
