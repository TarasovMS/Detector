package com.example.detector.presentation.ui.detectorScreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.detector.R
import com.example.detector.presentation.ui.detectorScreen.model.MainData

@Composable
fun NameTextField(
    modifier: Modifier = Modifier,
    data: MainData,
    textLabel: String,
) {
    OutlinedTextField(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        value = data.name,
        label = {
            Text(text = textLabel)
        },
        onValueChange = {
            data.name = it
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text
        ),
        isError = data.name.isEmpty(),
    )

    if (data.name.isEmpty()) {
        Text(
            modifier = modifier.padding(start = 16.dp),
            text = stringResource(id = R.string.name_field_error),
            color = colorResource(id = R.color.error_color),
            style = MaterialTheme.typography.caption
        )
    }
}
