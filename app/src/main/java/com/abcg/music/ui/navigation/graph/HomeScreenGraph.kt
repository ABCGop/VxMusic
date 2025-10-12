package com.abcg.music.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.abcg.music.ui.navigation.destination.home.CreditDestination
import com.abcg.music.ui.navigation.destination.home.MoodDestination
import com.abcg.music.ui.navigation.destination.home.NotificationDestination
import com.abcg.music.ui.navigation.destination.home.RecentlySongsDestination
import com.abcg.music.ui.navigation.destination.home.SettingsDestination
import com.abcg.music.ui.screen.home.MoodScreen
import com.abcg.music.ui.screen.home.NotificationScreen
import com.abcg.music.ui.screen.home.RecentlySongsScreen
import com.abcg.music.ui.screen.home.SettingScreen
import com.abcg.music.ui.screen.other.CreditScreen

@UnstableApi
fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<CreditDestination> {
        CreditScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<MoodDestination> { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<NotificationDestination> {
        NotificationScreen(
            navController = navController,
        )
    }
    composable<RecentlySongsDestination> {
        RecentlySongsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<SettingsDestination> {
        SettingScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}