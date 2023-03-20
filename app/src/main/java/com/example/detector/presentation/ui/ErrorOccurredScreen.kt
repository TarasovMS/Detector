package com.example.detector.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.detector.R

@Composable
fun ErrorOccurredScreen(
    modifier: Modifier = Modifier,
    error: Error,
    repeatOperationButton: () -> Unit,
) {

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            modifier = modifier
                .align(CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(id = R.string.failure_title),
            style = TextStyle(
                fontSize = 18.sp,
                color = MaterialTheme.colors.error,
            ),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Text(
            modifier = modifier
                .align(CenterHorizontally)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .weight(1.0f),
            text = error.message.orEmpty(),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )

        Button(
            modifier = modifier
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                .fillMaxWidth()
                .align(CenterHorizontally),
            onClick = {
                repeatOperationButton.invoke()
            },
        ) {
            Text(text = stringResource(R.string.ok))
        }
    }
}

@[Composable Preview(
    showBackground = true,
    device = "spec:width=411dp, height=891dp, dpi=420, isRound=false, chinSize=0dp, orientation=landscape"
)]
private fun ErrorOccurredScreenPreview() {
    ErrorOccurredScreen(
        modifier = Modifier,
        error = Error("Произошла ошибка")
    ) {
        /* no-op */
    }
}
