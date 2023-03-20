package com.example.detector.presentation.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.detector.presentation.ui.detectorScreen.DetectorScreen
import com.example.detector.presentation.ui.navigation.BottomNavigationPanelScreen.*

object BottomScreensNav {

    val bottomNavScreens by lazy {
        listOf(DetectorTextScreen, DetectorFaceScreen)
    }

    fun NavGraphBuilder.bottomNavigationScreens() {
        composable(DetectorTextScreen.route) {
            DetectorScreen()
        }

        composable(DetectorFaceScreen.route) {
            DetectorScreen()
        }
    }
}
