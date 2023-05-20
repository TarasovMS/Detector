package com.example.detector.presentation.ui.detectorScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.detector.presentation.ui.detectorScreen.model.MainData

@Composable
fun AddNameAlertDialog(
    modifier: Modifier = Modifier,
    data: MainData,
    onClickAdd: () -> Unit,
) {
    Dialog(
        onDismissRequest = { data.showDialog = false }
    ) {
        Surface(
            color = MaterialTheme.colors.surface,
        ) {
            Column(modifier = modifier.padding(16.dp)) {
                NameTextField(data = data, textLabel = "введите Имя")

                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            if (data.name.isNotEmpty()) {
                                onClickAdd.invoke()
                                data.showDialog = false
                            }
                        }
                    ) {
                        Text(text = "Добавить")
                    }
                }
            }
        }
    }
}
