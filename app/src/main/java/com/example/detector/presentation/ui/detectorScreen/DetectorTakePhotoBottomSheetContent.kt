package com.example.detector.presentation.ui.detectorScreen

import android.Manifest
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.detector.R
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiCallback
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileTakePhotoBottomSheetContent(detectorUiCallback: DetectorUiCallback) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val permissionsState = rememberMultiplePermissionsState(
            buildList {
                add(Manifest.permission.CAMERA)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        )

        detectorUiCallback.cameraPermissionGranted.invoke(
            permissionsState.allPermissionsGranted
        )

        Text(
            modifier = Modifier.padding(vertical = 12.dp),
            text = stringResource(id = R.string.photo_upload_methods),
            color = colorResource(R.color.cl_de767676),
            fontSize = 12.sp
        )

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.cl_de767676),
            thickness = 0.5.dp
        )

        PhotoBottomSheetRow(R.string.take_photo_from_camera, R.color.bottom_sheet_text_color) {
            if (!permissionsState.allPermissionsGranted)
                permissionsState.launchMultiplePermissionRequest()
            detectorUiCallback.camera.invoke()
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.cl_de767676),
            thickness = 0.5.dp
        )

        PhotoBottomSheetRow(R.string.take_photo_from_gallery, R.color.bottom_sheet_text_color) {
            if (!permissionsState.allPermissionsGranted)
                permissionsState.launchMultiplePermissionRequest()
            detectorUiCallback.gallery.invoke()
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.cl_de767676),
            thickness = 0.5.dp
        )

        PhotoBottomSheetRow(R.string.clear_photo, R.color.error_color) {
            detectorUiCallback.clearPhoto.invoke()
        }
    }
}

@Composable
fun PhotoBottomSheetRow(
    @StringRes text: Int,
    @ColorRes color: Int,
    itemClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                itemClick.invoke()
            },
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            color = colorResource(id = color),
            text = stringResource(id = text)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileTakePhotoBottomSheetContentPreview() {
    ProfileTakePhotoBottomSheetContent(
        DetectorUiCallback(
            cameraPermissionGranted = { /*no-op*/ },
            camera = { /*no-op*/ },
            gallery = { /*no-op*/ },
            clearPhoto = { /*no-op*/ }
        )
    )
}
