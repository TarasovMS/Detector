package com.example.detector.presentation.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.detector.presentation.ui.navigation.BottomNavigationPanelScreen.DetectorFaceScreen
import com.example.detector.presentation.ui.navigation.BottomScreensNav.bottomNavScreens
import com.example.detector.presentation.ui.navigation.BottomScreensNav.bottomNavigationScreens
import com.example.detector.presentation.ui.theme.FacesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandlerScreen()
        }
    }

    @Composable
    private fun HandlerScreen(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
    ) {

        FacesTheme {
            Surface(
                modifier = modifier.fillMaxSize(),
                color = MaterialTheme.colors.background,
            ) {
                MainScreen(navController = navController)
            }
        }

        initNavListener(
            navController = navController,
            title = {
                       //TODO
            },
        )
    }

    @Composable
    private fun MainScreen(
        modifier: Modifier = Modifier,
        navController: NavHostController,
    ) {
        Scaffold() { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = DetectorFaceScreen.route,
                modifier = modifier.padding(innerPadding)
            ) {
                bottomNavigationScreens()
            }
        }
    }

    private fun initNavListener(
        navController: NavController,
        title: (Int) -> Unit,
    ) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavScreens.find { it.route == destination.route }?.label?.let {
                title.invoke(it)
            }
        }
    }
}
