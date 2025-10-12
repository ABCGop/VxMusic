package com.abcg.music.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.abcg.music.ui.navigation.destination.login.LoginDestination
import com.abcg.music.ui.navigation.destination.login.SpotifyLoginDestination
import com.abcg.music.ui.screen.login.LoginScreen
import com.abcg.music.ui.screen.login.SpotifyLoginScreen

@UnstableApi
fun NavGraphBuilder.loginScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
    hideBottomBar: () -> Unit,
    showBottomBar: () -> Unit,
) {
    composable<LoginDestination> {
        LoginScreen(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomNavigation = hideBottomBar,
            showBottomNavigation = showBottomBar,
        )
    }

    composable<SpotifyLoginDestination> {
        SpotifyLoginScreen(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomNavigation = hideBottomBar,
            showBottomNavigation = showBottomBar,
        )
    }
}