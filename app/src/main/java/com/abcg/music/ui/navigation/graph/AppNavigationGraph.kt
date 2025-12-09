package com.abcg.music.ui.navigation.graph

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.abcg.music.ui.navigation.destination.home.HomeDestination
import com.abcg.music.ui.navigation.destination.library.LibraryDestination
import com.abcg.music.ui.navigation.destination.player.FullscreenDestination
import com.abcg.music.ui.navigation.destination.rang.RangDestination
import com.abcg.music.ui.navigation.destination.search.SearchDestination
import com.abcg.music.ui.screen.home.HomeScreen
import com.abcg.music.ui.screen.library.LibraryScreen
import com.abcg.music.ui.screen.other.SearchScreen
import com.abcg.music.ui.screen.player.FullscreenPlayer
import com.abcg.music.ui.screen.rang.RangScreen

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun AppNavigationGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    startDestination: Any = HomeDestination,
    hideNavBar: () -> Unit = { },
    showNavBar: (shouldShowNowPlayingSheet: Boolean) -> Unit = { },
    showNowPlayingSheet: () -> Unit = {},
    onScrolling: (onTop: Boolean) -> Unit = {},
) {
    NavHost(
        navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn() + slideInHorizontally { -it }
        },
        exitTransition = {
            fadeOut() + slideOutHorizontally { it }
        },
        popEnterTransition = {
            fadeIn() + slideInHorizontally { -it }
        },
        popExitTransition = {
            fadeOut() + slideOutHorizontally { it }
        },
    ) {
        // Bottom bar destinations
        composable<HomeDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://home" })
        ) {
            HomeScreen(
                onScrolling = onScrolling,
                navController = navController,
            )
        }
        composable<SearchDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://search" })
        ) {
            SearchScreen(
                navController = navController,
            )
        }

        composable<LibraryDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://library" })
        ) {
            LibraryScreen(
                innerPadding = innerPadding,
                navController = navController,
                onScrolling = onScrolling,
            )
        }
        composable<RangDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://rang" })
        ) {
            RangScreen(
                navController = navController,
            )
        }
        
        // Profile screen
        composable(
            route = "profile?userId={userId}",
            arguments = listOf(androidx.navigation.navArgument("userId") { 
                nullable = true 
                defaultValue = null 
            }),
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://profile?userId={userId}" })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            showNavBar(false)
            com.abcg.music.ui.screen.rang.ProfileScreen(
                navController = navController,
                userId = userId
            )
        }
        
        // Chat screen
        composable(
            route = "chat/{userId}/{username}",
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://chat/{userId}/{username}" })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            
            DisposableEffect(Unit) {
                hideNavBar()
                onDispose {
                    showNavBar(false)
                }
            }
            
            com.abcg.music.ui.screen.rang.ChatScreen(
                navController = navController,
                otherUserId = userId,
                otherUsername = username
            )
        }
        
        composable<FullscreenDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://player" })
        ) {
            FullscreenPlayer(
                navController,
                hideNavBar = hideNavBar,
                showNavBar = {
                    showNavBar.invoke(true)
                    showNowPlayingSheet.invoke()
                },
            )
        }
        // Home screen graph
        homeScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        // Library screen graph
        libraryScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        // List screen graph
        listScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        // Login screen graph
        loginScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomBar = hideNavBar,
            showBottomBar = {
                showNavBar(false)
            },
        )

        // Subscription Screens removed

        composable<com.abcg.music.ui.navigation.destination.home.ThemePickerDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://theme" })
        ) {
            com.abcg.music.ui.screen.theme.ThemePickerScreen(
                navController = navController
            )
        }

        composable<com.abcg.music.ui.navigation.destination.home.IconPickerDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://icon" })
        ) {
            com.abcg.music.ui.screen.icon.IconPickerScreen(
                navController = navController
            )
        }

        composable<com.abcg.music.ui.navigation.destination.home.WrappedDestination>(
            deepLinks = listOf(androidx.navigation.navDeepLink { uriPattern = "vxmusic://wrapped" })
        ) {
            com.abcg.music.ui.screen.wrapped.WrappedScreen(
                navController = navController
            )
        }
    }
}
