package com.abcg.music.ui.utils

import androidx.compose.ui.util.fastAny
import androidx.navigation.NavController
import com.abcg.music.ui.screens.Screens

fun NavController.backToMain() {
    while (!Screens.MainScreens.fastAny { it.route == currentBackStackEntry?.destination?.route }) {
        navigateUp()
    }
}