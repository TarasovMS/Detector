package com.example.detector.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.detector.R
import com.example.detector.common.BaseViewModel
import com.example.detector.common.contextProvider.ResourceProviderContext
import com.example.detector.domain.DetectorUseCase
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
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
    resourceProviderContext: ResourceProviderContext
) : BaseViewModel<DetectorScreenTriggerEvent>() {

    private val bitmapImageStateFlow = MutableStateFlow<Bitmap?>(
        BitmapFactory.decodeResource(
            resourceProviderContext.getContext().resources,
            R.drawable.test_photo
        )
    )
    private val uriForNewPhotoStateFlow = MutableStateFlow<Uri>(Uri.EMPTY)
    private val faceListStateFlow = MutableStateFlow<List<Face>>(emptyList())

    private val _detectorUiStateFlow = MutableStateFlow<DetectorScreenState>(DetectorScreenProgress)
    val detectorUiStateFlow = _detectorUiStateFlow.asStateFlow()

    init {
        getUriForPhoto()
        detectorUseCase.createTempFilesForPhotos()
        collectErrorFlow()
        combineFlows()
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
                    bitmapImageStateFlow.value = it
                }
            )
        }
    }

    private fun clearPhoto() {
        bitmapImageStateFlow.value = null
    }

    private fun addImageToGalleryAndPostPhoto() {
        _detectorUiStateFlow.value = DetectorScreenProgress
        executeSuspend {
            detectorUseCase.addImageToGalleryAndPost().fold(
                ifLeft = ::handleError,
                ifRight = {
                    bitmapImageStateFlow.value = it
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
        executeSuspend(Dispatchers.Main) {
            combine(
                bitmapImageStateFlow,
                uriForNewPhotoStateFlow,
                faceListStateFlow,
            ) { photoBitmap, uriForNewPhoto, faceList ->
                DetectorUiData(
                    photoBitmapInit = photoBitmap,
                    uriNewPhotoInit = uriForNewPhoto,
                    faceListInit = faceList,
                )
            }.collect { data ->
                _detectorUiStateFlow.value = DetectorScreenLoadComplete(data)
            }
        }
    }
}
