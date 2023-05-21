package com.example.detector.presentation.ui.detectorScreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import com.example.detector.presentation.ui.detectorScreen.model.DetectorUiData
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenState.*
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent
import com.example.detector.presentation.ui.detectorScreen.state.DetectorScreenTriggerEvent.RepeatData
import com.example.detector.presentation.viewmodel.DetectorViewModel

@Composable
fun DetectorScreen(viewModel: DetectorViewModel = hiltViewModel()) {

    val uiState by viewModel.detectorUiStateFlow.collectAsState()

    DetectorScreenHandler(
        uiState = uiState,
        uiTrigger = {
            viewModel.onTriggerEvent(it)
        },
    )
}

@Composable
fun DetectorScreenHandler(
    uiState: DetectorScreenState,
    uiTrigger: (DetectorScreenTriggerEvent) -> Unit,
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
                        uiTrigger.invoke(RepeatData(it))
                    }
                },
            ) {
                Text(text = stringResource(R.string.ok))
            }

            data.detectorBitmaps.faceBitmap?.let {
                DetectorFace(
                    modifier = modifier.fillMaxWidth(),
                    bitmap = it,
                    data = data,
                )

                DetectorFaceContour(
                    modifier = modifier.fillMaxSize(),
                    data = data
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetectorScreenContentPreview() {
    DetectorScreenContent(
        data = DetectorUiData(),
        uiTrigger = {},
        onClickChangePhoto = {},
    )
}
