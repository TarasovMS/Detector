package com.example.detector.presentation.ui.navigation

sealed class NavigationDirection {
    object CalculatorToRegistrationFirstNavDir : NavigationDirection()
    object CalculatorToAuthenticationNavDir : NavigationDirection()
}