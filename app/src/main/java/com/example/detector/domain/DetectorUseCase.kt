package com.example.detector.domain

import android.graphics.*
import android.net.Uri
import arrow.core.Either
import arrow.core.right
import com.google.mlkit.vision.face.Face
import javax.inject.Inject

class DetectorUseCase @Inject constructor(
    private val detectorRepository: DetectorRepository,
) {

    val modelFile = "mobile_face_net.tflite"

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

            Bitmap.createScaledBitmap(faceBitmap, 112, 112, true)      //TODO(112)
        } else {
            null
        }).right()
//        firstOrNull
    }

    fun recognizeImage(bitmap: Bitmap) {
        // set Face to Preview
//        face_preview.setImageBitmap(bitmap)

        //Create ByteBuffer to store normalized image
//        val imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
//        imgData.order(ByteOrder.nativeOrder())
//        intValues = IntArray(inputSize * inputSize)
//
//        //get pixel values from Bitmap to normalize
//        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
//        imgData.rewind()
//        for (i in 0 until inputSize) {
//            for (j in 0 until inputSize) {
//                val pixelValue: Int = intValues.get(i * inputSize + j)
//                if (isModelQuantized) {
//                    // Quantized model
//                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
//                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
//                    imgData.put((pixelValue and 0xFF).toByte())
//                } else { // Float model
//                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
//                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
//                    imgData.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
//                }
//            }
//        }
//        //imgData is input to our model
//        val inputArray = arrayOf<Any>(imgData)
//        val outputMap: MutableMap<Int, Any> = HashMap()
//        embeedings =
//            Array(1) { FloatArray(OUTPUT_SIZE) } //output of model will be stored in this variable
//        outputMap[0] = embeedings
//        tfLite.runForMultipleInputsOutputs(inputArray, outputMap) //Run model
//        var distance_local = Float.MAX_VALUE
//        val id = "0"
//        val label = "?"

        //Compare new face with saved Faces.
//        if (registered.size > 0) {
//            val nearest: List<Pair<String, Float>?> =
//                findNearest(embeedings.get(0)) //Find 2 closest matching face
//            if (nearest[0] != null) {
//                val name = nearest[0]!!.first //get name and distance of closest matching face
//                // label = name;
//                distance_local = nearest[0]!!.second
//                if (developerMode) {
//                    if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
//                        reco_name.setText(
//                            """
//                        Nearest: $name
//                        Dist: ${String.format("%.3f", distance_local)}
//                        2nd Nearest: ${nearest[1]!!.first}
//                        Dist: ${String.format("%.3f", nearest[1]!!.second)}
//                        """.trimIndent()
//                        ) else reco_name.setText(
//                        """
//                        Unknown
//                        Dist: ${String.format("%.3f", distance_local)}
//                        Nearest: $name
//                        Dist: ${String.format("%.3f", distance_local)}
//                        2nd Nearest: ${nearest[1]!!.first}
//                        Dist: ${String.format("%.3f", nearest[1]!!.second)}
//                        """.trimIndent()
//                    )
//
////                    System.out.println("nearest: " + name + " - distance: " + distance_local);
//                } else {
//                    if (distance_local < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
//                        reco_name.setText(name) else reco_name.setText("Unknown")
//                    //                    System.out.println("nearest: " + name + " - distance: " + distance_local);
//                }
//            }
//        }
    }


    //    public void register(String name, SimilarityClassifier.Recognition rec) {
    //        registered.put(name, rec);
    //    }
//    private fun findNearest(emb: FloatArray): List<Pair<String, Float>?>? {
//        val neighbour_list: MutableList<Pair<String, Float>?> = ArrayList()
//        var ret: Pair<String, Float>? = null //to get closest match
//        var prev_ret: Pair<String, Float>? = null //to get second closest match
//        for ((name, value): Map.Entry<String, SimilarityClassifier.Recognition> in registered.entries) {
//            val knownEmb = value.getExtra()[0]
//            var distance = 0f
//            for (i in emb.indices) {
//                val diff = emb[i] - knownEmb[i]
//                distance += diff * diff
//            }
//            distance = Math.sqrt(distance.toDouble()).toFloat()
//            if (ret == null || distance < ret.second) {
//                prev_ret = ret
//                ret = Pair(name, distance)
//            }
//        }
//        if (prev_ret == null) prev_ret = ret
//        neighbour_list.add(ret)
//        neighbour_list.add(prev_ret)
//        return neighbour_list
//    }

}
