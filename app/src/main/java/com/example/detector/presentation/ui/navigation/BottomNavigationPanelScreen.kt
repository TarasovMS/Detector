package com.example.detector.presentation.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.detector.R

sealed class BottomNavigationPanelScreen(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
)  {

    object DetectorTextScreen : BottomNavigationPanelScreen(
        route = "Detector_text_screen",
        label = R.string.ok,
        icon = R.drawable.ic_add
    )

    object DetectorFaceScreen : BottomNavigationPanelScreen(
        route = "Detector_face_screen",
        label = R.string.ok,
        icon = R.drawable.ic_add,
    )

}
