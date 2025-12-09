package com.abcg.music.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
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

fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<CreditDestination>(
        deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://credit" })
    ) {
        CreditScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<MoodDestination>(
        // Assuming params matches the query or path property
        deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://mood/{params}" })
    ) { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<NotificationDestination>(
        deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://notifications" })
    ) {
        NotificationScreen(
            navController = navController,
        )
    }
    composable<RecentlySongsDestination>(
         deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://recent" })
    ) {
        RecentlySongsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<SettingsDestination>(
        deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://settings" })
    ) {
        SettingScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<com.abcg.music.ui.navigation.destination.home.WrappedDestination>(
         deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://wrapped" })
    ) {
        com.abcg.music.ui.screen.wrapped.WrappedScreen(
            navController = navController,
            viewModel = org.koin.androidx.compose.koinViewModel()
        )
    }
}
