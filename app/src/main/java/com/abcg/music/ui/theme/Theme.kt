package com.abcg.music.ui.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.toColorInt
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import okio.FileSystem

// ============================================
// VxMusic Color Scheme - Modern Dark Theme
// Neon Purple/Blue Premium Design
// ============================================
val DarkColors =
    darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        error = md_theme_dark_error,
        errorContainer = md_theme_dark_errorContainer,
        onError = md_theme_dark_onError,
        onErrorContainer = md_theme_dark_onErrorContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        outline = md_theme_dark_outline,
        inverseOnSurface = md_theme_dark_inverseOnSurface,
        inverseSurface = md_theme_dark_inverseSurface,
        inversePrimary = md_theme_dark_inversePrimary,
        surfaceTint = md_theme_dark_surfaceTint,
        outlineVariant = md_theme_dark_outlineVariant,
        scrim = md_theme_dark_scrim,
    )

@Composable
fun AppTheme(
    themeId: String? = null,
    content:
        @Composable()
        () -> Unit,
) {
    val context = LocalContext.current
    
    // Get admin theme colors from SharedPreferences
    val prefs = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
    val themeUpdateTime = prefs.getLong("theme_updated_at", 0L)
    
    // Read colors - will recompose when themeUpdateTime changes
    val primaryColorHex = remember(themeUpdateTime) { prefs.getString("primary_color", null) }
    val secondaryColorHex = remember(themeUpdateTime) { prefs.getString("secondary_color", null) }
    val accentColorHex = remember(themeUpdateTime) { prefs.getString("accent_color", null) }
    val backgroundColorHex = remember(themeUpdateTime) { prefs.getString("background_color", null) }
    val surfaceColorHex = remember(themeUpdateTime) { prefs.getString("surface_color", null) }
    val errorColorHex = remember(themeUpdateTime) { prefs.getString("error_color", null) }
    
    // Apply custom colors if available
    val colors = if (themeId != null && themeId != "default") {
        // Use Premium Theme
        val theme = com.abcg.music.ui.theme.ThemeManager.getThemeById(themeId)
        darkColorScheme(
            primary = Color(theme.primaryColor),
            onPrimary = Color(theme.onPrimary),
            primaryContainer = Color(theme.primaryColor).copy(alpha = 0.2f),
            onPrimaryContainer = Color(theme.primaryColor),
            secondary = Color(theme.secondaryColor),
            onSecondary = Color(theme.onSecondary),
            secondaryContainer = Color(theme.secondaryColor).copy(alpha = 0.2f),
            onSecondaryContainer = Color(theme.secondaryColor),
            tertiary = Color(theme.accentColor),
            onTertiary = Color.White,
            tertiaryContainer = Color(theme.accentColor).copy(alpha = 0.2f),
            onTertiaryContainer = Color(theme.accentColor),
            background = Color(theme.backgroundColor),
            onBackground = Color(theme.onBackground),
            surface = Color(theme.surfaceColor),
            onSurface = Color(theme.onBackground), // Use onBackground for onSurface usually
            surfaceVariant = Color(theme.surfaceColor).copy(alpha = 0.8f),
            onSurfaceVariant = Color(theme.onBackground).copy(alpha = 0.8f),
            outline = Color(theme.onBackground).copy(alpha = 0.3f),
            inverseOnSurface = Color(theme.backgroundColor),
            inverseSurface = Color(theme.onBackground),
            inversePrimary = Color(theme.primaryColor),
            surfaceTint = Color(theme.primaryColor),
            outlineVariant = Color(theme.onBackground).copy(alpha = 0.1f),
            scrim = Color.Black,
        )
    } else if (primaryColorHex != null) {
        try {
            val primaryColor = Color(primaryColorHex.toColorInt())
            val secondaryColor = if (secondaryColorHex != null) Color(secondaryColorHex.toColorInt()) else Color(0xFF3B82F6)
            val accentColor = if (accentColorHex != null) Color(accentColorHex.toColorInt()) else secondaryColor
            val backgroundColor = if (backgroundColorHex != null) Color(backgroundColorHex.toColorInt()) else md_theme_dark_background
            val surfaceColor = if (surfaceColorHex != null) Color(surfaceColorHex.toColorInt()) else md_theme_dark_surface
            val errorColor = if (errorColorHex != null) Color(errorColorHex.toColorInt()) else md_theme_dark_error
            
            darkColorScheme(
                primary = primaryColor,
                onPrimary = Color.White,
                primaryContainer = primaryColor.copy(alpha = 0.2f),
                onPrimaryContainer = primaryColor,
                secondary = secondaryColor,
                onSecondary = Color.White,
                secondaryContainer = secondaryColor.copy(alpha = 0.2f),
                onSecondaryContainer = secondaryColor,
                tertiary = accentColor,
                onTertiary = Color.White,
                tertiaryContainer = accentColor.copy(alpha = 0.2f),
                onTertiaryContainer = accentColor,
                error = errorColor,
                errorContainer = errorColor.copy(alpha = 0.2f),
                onError = Color.White,
                onErrorContainer = errorColor,
                background = backgroundColor,
                onBackground = Color.White,
                surface = surfaceColor,
                onSurface = Color.White,
                surfaceVariant = surfaceColor.copy(alpha = 0.8f),
                onSurfaceVariant = Color.White.copy(alpha = 0.8f),
                outline = Color.White.copy(alpha = 0.3f),
                inverseOnSurface = backgroundColor,
                inverseSurface = Color.White,
                inversePrimary = primaryColor,
                surfaceTint = primaryColor,
                outlineVariant = Color.White.copy(alpha = 0.1f),
                scrim = Color.Black,
            )
        } catch (e: Exception) {
            DarkColors
        }
    } else {
        DarkColors
    }
    
    val contentWithImageLoader: @Composable () -> Unit = {
        setSingletonImageLoaderFactory { context ->
            ImageLoader
                .Builder(context)
                .logger(DebugLogger())
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .diskCache(newDiskCache())
                .crossfade(true)
                .build()
        }
        content()
    }

    MaterialTheme(
        colorScheme = colors,
        content = {
            CompositionLocalProvider(
                LocalContentColor provides colors.onSurfaceVariant,
                contentWithImageLoader,
            )
        },
        typography = typo,
    )
}

fun supportsDynamic(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) true else false

fun newDiskCache(): DiskCache =
    DiskCache
        .Builder()
        .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(512L * 1024 * 1024)
        .build()

@Composable
@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Light Mode",
)
fun AppThemePreview() {
    AppTheme {
        Column {
            Text(text = "Hello, World!", style = typo.titleSmall)
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Click me!")
            }
        }
    }
}
