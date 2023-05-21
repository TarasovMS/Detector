package com.example.detector.presentation.ui.detectorScreen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.detector.R
import com.example.detector.common.EMPTY_STRING
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
import com.example.detector.presentation.ui.detectorScreen.model.FaceRecognition

@Composable
fun DetectorFace(
    modifier: Modifier = Modifier,
    bitmap: Bitmap,
    data: DetectorUiData,
) {
    with(data) {
        val painter = rememberAsyncImagePainter(bitmap)

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Image(
                modifier = modifier.size(200.dp),
                painter = painter,
                contentDescription = EMPTY_STRING,
            )

            Text(text = nearestName.name)

            TextButton(
                onClick = { mainData.showDialog = true }
            ) {
                Text(text = stringResource(id = R.string.add_face))
            }

            if (mainData.showDialog) {
                AddNameAlertDialog(
                    data = mainData,
                    onClickAdd = {
                        faceData.faceRecognition.add(
                            FaceRecognition(mainData.name, faceData.faceModelData)
                        )
                    }
                )
            }
        }
    }
}
