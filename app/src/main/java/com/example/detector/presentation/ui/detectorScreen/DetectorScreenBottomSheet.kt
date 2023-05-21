package com.example.detector.presentation.ui.detectorScreen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiCallback
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DetectorScreenBottomSheet(
    data: DetectorUiData,
    uiTrigger: (DetectorScreenTriggerEvent) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )

    val cameraPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { shootSuccess ->
            if (shootSuccess)
                uiTrigger.invoke(UploadPhotoFromCamera)
        }
    )

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                uiTrigger.invoke(UploadPhotoFromPicker(it))
            }
        }
    )

    BackHandler(bottomSheetState.isVisible) {
        coroutineScope.launch { bottomSheetState.hide() }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetBackgroundColor = MaterialTheme.colors.surface,
        modifier = Modifier.fillMaxWidth(),
        sheetContent = {
            ProfileTakePhotoBottomSheetContent(
                detectorUiCallback = DetectorUiCallback(
                    cameraPermissionGranted = {
                        cameraPermissionGranted = it
                    },
                    camera = {
                        if (cameraPermissionGranted) {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                            }

                            cameraPicker.launch(
                                data.detectorBitmaps.uriNewPhoto
                            )
                        }
                    },
                    gallery = {
                        if (cameraPermissionGranted) {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                            }
                            photoPicker.launch(PickVisualMediaRequest(ImageOnly))
                        }
                    },
                    clearPhoto = {
                        coroutineScope.launch {
                            bottomSheetState.hide()
                        }
                        uiTrigger.invoke(ClearPhoto)
                    }
                )
            )
        }
    ) {
        DetectorScreenContent(
            data = data,
            uiTrigger = uiTrigger,
            onClickChangePhoto = {
                coroutineScope.launch {
                    bottomSheetState.show()
                }
            },
        )
    }
}
