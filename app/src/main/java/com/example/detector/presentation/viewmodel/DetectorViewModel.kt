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
import com.example.detector.presentation.ui.detectorScreen.model.FaceData
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DetectorViewModel @Inject constructor(
    private val detectorUseCase: DetectorUseCase,
    contextProvider: ResourceProviderContext
) : BaseViewModel<DetectorScreenTriggerEvent>() {

    private val uiDataStateFlow = MutableStateFlow(DetectorUiData())
    private val faceDataStateFlow = MutableStateFlow(FaceData())
    private val nearestNameStateFlow = MutableStateFlow(NearestName())
    private val mainDataStateFlow = MutableStateFlow(MainData())
    private val detectorBitmapsStateFlow = MutableStateFlow(
        DetectorBitmaps(
            photoBitmap = BitmapFactory.decodeResource(
                contextProvider.getContext().resources,
                R.drawable.test_photo
            )
        )
    )

    private val _detectorUiStateFlow = MutableStateFlow<DetectorScreenState>(DetectorScreenProgress)
    val detectorUiStateFlow = _detectorUiStateFlow.asStateFlow()

    init {
        collectErrorFlow()
        combineFlows()
        getUriForPhoto()
    }

    override fun onTriggerEvent(eventType: DetectorScreenTriggerEvent) {
        when (eventType) {
            is ClearPhoto -> clearPhoto()
            is UploadPhotoFromCamera -> addImageToGalleryAndPostPhoto()
            is UploadPhotoFromPicker -> getImageFromGalleryAndPost(eventType.uri)
            is RepeatData -> runFaceContourDetection(eventType.bitmap)
        }
    }

    override fun collectErrorFlow() {
        executeSuspend {
            errorFlow.collect {
                _detectorUiStateFlow.updateErrorState(DetectorScreenError(it))
            }
        }
    }

    private fun runFaceContourDetection(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .build()

        FaceDetection
            .getClient(options)
            .process(image)
            .addOnSuccessListener { faces ->
                faceDataStateFlow.value.faceList = faces
                handleDetector(faces, bitmap)
            }
            .addOnFailureListener { e ->
                //TODO Task failed with an exception
                e.printStackTrace()
            }
    }

    private fun handleDetector(faces: List<Face>, bitmap: Bitmap) {
        detectorUseCase.handleDetector(faces, bitmap).fold(
            ifLeft = ::handleError,
            ifRight = { photoBitmap ->
                detectorBitmapsStateFlow.update {
                    it.copy(faceBitmap = photoBitmap)
                }

                photoBitmap?.let {
                    recognizeImage(it)
                }
            }
        )
    }

    private fun recognizeImage(bitmap: Bitmap) {
        detectorUseCase.recognizeImage(bitmap, uiDataStateFlow.value).fold(
            ifLeft = ::handleError,
            ifRight = { name ->
                nearestNameStateFlow.value = NearestName(name)
            }
        )
    }

    private fun getImageFromGalleryAndPost(uri: Uri) {
        executeSuspend {
            detectorUseCase.getImageFromGalleryAndPost(uri).fold(
                ifLeft = ::handleError,
                ifRight = { bitmap ->
                    detectorBitmapsStateFlow.update {
                        it.copy(photoBitmap = bitmap)
                    }
                }
            )
        }
    }

    private fun clearPhoto() {
        detectorBitmapsStateFlow.update {
            it.copy(photoBitmap = null)
        }
    }

    private fun addImageToGalleryAndPostPhoto() {
        executeSuspend {
            detectorUseCase.addImageToGalleryAndPost().fold(
                ifLeft = ::handleError,
                ifRight = { bitmap ->
                    detectorBitmapsStateFlow.update {
                        it.copy(photoBitmap = bitmap)
                    }
                }
            )
        }
    }

    private fun getUriForPhoto() {
        executeSuspend {
            detectorUseCase.getUriForPhoto().fold(
                ifLeft = ::handleError,
                ifRight = { uri ->
                    detectorBitmapsStateFlow.update {
                        it.copy(uriNewPhoto = uri)
                    }
                }
            )
        }
    }

    private fun combineFlows() {
        executeSuspend {
            combine(
                detectorBitmapsStateFlow,
                faceDataStateFlow,
                nearestNameStateFlow,
                mainDataStateFlow,
            ) { detectorBitmaps, faceData, nearestName, mainData ->
                DetectorUiData(
                    detectorBitmaps = detectorBitmaps,
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
}
