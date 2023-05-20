package com.example.detector.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.detector.R
import com.example.detector.common.BaseViewModel
import com.example.detector.common.contextProvider.ResourceProviderContext
import com.example.detector.domain.DetectorUseCase
import com.example.detector.presentation.ui.detectorScreen.model.DetectorBitmaps
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
import com.example.detector.presentation.ui.detectorScreen.model.Embeedings
import com.example.detector.presentation.ui.detectorScreen.model.FaceData
import com.example.detector.presentation.ui.detectorScreen.model.FaceRecognition
import com.example.detector.presentation.ui.detectorScreen.model.NearestName
import com.example.detector.presentation.ui.detectorScreen.model.MainData
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState.*
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class DetectorViewModel @Inject constructor(
    private val detectorUseCase: DetectorUseCase,
    contextProvider: ResourceProviderContext
) : BaseViewModel<DetectorScreenTriggerEvent>() {

    private val imageBitmapStateFlow = MutableStateFlow<Bitmap?>(
        BitmapFactory.decodeResource(
            contextProvider.getContext().resources,
            R.drawable.test_photo
        )
    )
    private val faceBitmapStateFlow = MutableStateFlow<Bitmap?>(null)
    private val uriForNewPhotoStateFlow = MutableStateFlow<Uri>(Uri.EMPTY)
    private val faceListStateFlow = MutableStateFlow<List<Face>>(emptyList())
    private val faceRecognitionStateFlow = MutableStateFlow(arrayListOf<FaceRecognition>())
    private val uiDataStateFlow = MutableStateFlow(DetectorUiData())

    private val detectorBitmapsStateFlow = MutableStateFlow(DetectorBitmaps())
    private val embeedingsStateFlow = MutableStateFlow(Embeedings())
    private val faceDataStateFlow = MutableStateFlow(FaceData())
    private val nearestNameStateFlow = MutableStateFlow(NearestName())
    private val mainDataStateFlow = MutableStateFlow(MainData())

    private val _detectorUiStateFlow = MutableStateFlow<DetectorScreenState>(DetectorScreenProgress)
    val detectorUiStateFlow = _detectorUiStateFlow.asStateFlow()

    init {
        getUriForPhoto()
        detectorUseCase.createTempFilesForPhotos()
        collectErrorFlow()

        combineFlows()
        combineDetectorBitmapsFlows()
        combineFaceDataFlows()
    }

    override fun onTriggerEvent(eventType: DetectorScreenTriggerEvent) {
        when (eventType) {
            is ClearPhoto -> clearPhoto()
            is UploadPhotoFromCamera -> addImageToGalleryAndPostPhoto()
            is UploadPhotoFromPicker -> getImageFromGalleryAndPost(eventType.uri)
            is RepeatData -> {
                runFaceContourDetection(eventType.bitmap)
            }
        }
    }

    override fun collectErrorFlow() {
        executeSuspend {
            errorFlow.collect {
                _detectorUiStateFlow.updateErrorState(DetectorScreenError(it))
            }
        }
    }

    fun runFaceContourDetection(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)
        detector.process(image)
            .addOnSuccessListener { faces ->
//                processFaceContourDetectionResult(faces)
                faceListStateFlow.value = faces

                detectorUseCase.handleDetector(faces, bitmap).fold(
                    ifLeft = ::handleError,
                    ifRight = { bitmap ->
                        faceBitmapStateFlow.value = bitmap

                        bitmap?.let {
                            detectorUseCase.recognizeImage(bitmap, uiDataStateFlow.value).fold(
                                ifLeft = ::handleError,
                                ifRight = { name ->
                                    nearestNameStateFlow.value = NearestName(name)
                                }
                            )
                        }
                    }
                )
            }
            .addOnFailureListener { e ->
                //TODO Task failed with an exception
                e.printStackTrace()
            }
    }


//    private fun processFaceContourDetectionResult(faces: List<Face>) {
//        // Task completed successfully
//        if (faces.isEmpty()) {
//            showToast("No face found")
//            return
//        }
//
//        mGraphicOverlay.clear()
//
//        for (i in faces.indices) {
//            val face = faces[i]
//            val faceGraphic = FaceContourGraphic(mGraphicOverlay)
//            mGraphicOverlay.add(faceGraphic)
//            faceGraphic.updateFace(face)
//        }
//    }

    private fun getImageFromGalleryAndPost(uri: Uri) {
        _detectorUiStateFlow.value = DetectorScreenProgress
        executeSuspend {
            detectorUseCase.getImageFromGalleryAndPost(uri).fold(
                ifLeft = ::handleError,
                ifRight = {
                    imageBitmapStateFlow.value = it
                }
            )
        }
    }

    private fun clearPhoto() {
        imageBitmapStateFlow.value = null
    }

    private fun addImageToGalleryAndPostPhoto() {
        _detectorUiStateFlow.value = DetectorScreenProgress
        executeSuspend {
            detectorUseCase.addImageToGalleryAndPost().fold(
                ifLeft = ::handleError,
                ifRight = {
                    imageBitmapStateFlow.value = it
                }
            )
        }
    }

    private fun getUriForPhoto() {
        executeSuspend {
            detectorUseCase.getUriForPhoto().fold(
                ifLeft = ::handleError,
                ifRight = {
                    uriForNewPhotoStateFlow.value = it
                }
            )
        }
    }

    private fun combineFlows() {
        executeSuspend(
            Dispatchers.Main
        ) {
            combine(
                detectorBitmapsStateFlow,
                embeedingsStateFlow,
                faceDataStateFlow,
                nearestNameStateFlow,
                mainDataStateFlow,
            ) { detectorBitmaps, embeedings, faceData, nearestName, mainData ->
                DetectorUiData(
                    detectorBitmaps = detectorBitmaps,
                    embeedingsData = embeedings,
                    faceData = faceData,
                    nearestName = nearestName,
                    mainData = mainData,
                )
            }.collect { data ->
                uiDataStateFlow.value = data
                _detectorUiStateFlow.value = DetectorScreenLoadComplete(data)
            }
        }
    }

    private fun combineDetectorBitmapsFlows() {
        executeSuspend {
            combine(
                faceBitmapStateFlow,
                uriForNewPhotoStateFlow,
                imageBitmapStateFlow,
            ) { faceBitmap, uriForNewPhoto, imageBitmap ->
                DetectorBitmaps(
                    photoBitmapInit = imageBitmap,
                    faceBitmapInit = faceBitmap,
                    uriNewPhotoInit = uriForNewPhoto
                )
            }.collect {
                detectorBitmapsStateFlow.value = it
            }
        }
    }

    private fun combineFaceDataFlows() {
        executeSuspend {
            combine(
                faceListStateFlow,
                faceRecognitionStateFlow,
            ) { faceList, faceRecognition ->
                FaceData(
                    faceListInit = faceList,
                    faceRecognitionInit = faceRecognition,
                )
            }.collect {
                faceDataStateFlow.value = it
            }
        }
    }
}
