package com.abcg.music.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Adaptive layout component that adjusts based on screen size and orientation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveLayout(
    windowSizeClass: WindowSizeClass,
    navigationContent: @Composable () -> Unit,
    playerContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    
    // Determine layout type based on screen characteristics
    val layoutType = when {
        screenWidth >= 960 -> LayoutType.TV
        screenWidth >= 600 && isLandscape -> LayoutType.TABLET_LANDSCAPE
        screenWidth >= 600 -> LayoutType.TABLET_PORTRAIT
        isLandscape -> LayoutType.PHONE_LANDSCAPE
        else -> LayoutType.PHONE_PORTRAIT
    }

    when (layoutType) {
        LayoutType.TV -> TVLayout(
            navigationContent = navigationContent,
            playerContent = playerContent,
            mainContent = mainContent,
            modifier = modifier
        )
        
        LayoutType.TABLET_LANDSCAPE -> TabletLandscapeLayout(
            navigationContent = navigationContent,
            playerContent = playerContent,
            mainContent = mainContent,
            modifier = modifier
        )
        
        LayoutType.TABLET_PORTRAIT -> TabletPortraitLayout(
            navigationContent = navigationContent,
            playerContent = playerContent,
            mainContent = mainContent,
            modifier = modifier
        )
        
        LayoutType.PHONE_LANDSCAPE -> PhoneLandscapeLayout(
            navigationContent = navigationContent,
            playerContent = playerContent,
            mainContent = mainContent,
            modifier = modifier
        )
        
        LayoutType.PHONE_PORTRAIT -> PhonePortraitLayout(
            navigationContent = navigationContent,
            playerContent = playerContent,
            mainContent = mainContent,
            modifier = modifier
        )
    }
}

/**
 * TV Layout - Side navigation with large player area
 */
@Composable
private fun TVLayout(
    navigationContent: @Composable () -> Unit,
    playerContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left sidebar navigation (20%)
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(240.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            navigationContent()
        }
        
        // Main content area (50%)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            mainContent()
        }
        
        // Right sidebar player (30%)
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            playerContent()
        }
    }
}

/**
 * Tablet Landscape Layout - Navigation drawer with split view
 */
@Composable
private fun TabletLandscapeLayout(
    navigationContent: @Composable () -> Unit,
    playerContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left navigation (25%)
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.25f),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            navigationContent()
        }
        
        // Main content (50%)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    mainContent()
                }
                
                // Bottom player bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    playerContent()
                }
            }
        }
        
        // Right content/queue (25%)
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.33f),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            // Queue or additional content
        }
    }
}

/**
 * Tablet Portrait Layout - Bottom navigation with expanded player
 */
@Composable
private fun TabletPortraitLayout(
    navigationContent: @Composable () -> Unit,
    playerContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            mainContent()
        }
        
        // Expanded player area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            playerContent()
        }
        
        // Bottom navigation
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            navigationContent()
        }
    }
}

/**
 * Phone Landscape Layout - Optimized for landscape orientation
 */
@Composable
private fun PhoneLandscapeLayout(
    navigationContent: @Composable () -> Unit,
    playerContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // Left navigation rail (compact)
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            navigationContent()
        }
        
        // Main content with overlay player
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            mainContent()
            
            // Overlay player at bottom
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f)
            ) {
                playerContent()
            }
        }
    }
}

/**
 * Phone Portrait Layout - Standard phone layout
 */
@Composable
private fun PhonePortraitLayout(
    navigationContent: @Composable () -> Unit,
    playerContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            mainContent()
        }
        
        // Player bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            playerContent()
        }
        
        // Bottom navigation
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            navigationContent()
        }
    }
}

enum class LayoutType {
    TV,
    TABLET_LANDSCAPE,
    TABLET_PORTRAIT,
    PHONE_LANDSCAPE,
    PHONE_PORTRAIT
}