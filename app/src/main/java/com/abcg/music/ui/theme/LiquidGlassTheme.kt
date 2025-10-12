package com.abcg.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Liquid Glass Theme Variants (BETA)
 */
object LiquidGlassTheme {
    
    @Composable
    fun LiquidGlassColorScheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = false
    ): ColorScheme {
        val context = LocalContext.current
        
        return when {
            dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                if (darkTheme) {
                    dynamicDarkColorScheme(context).copy(
                        surface = Color.Black.copy(alpha = 0.3f),
                        surfaceContainer = Color.Black.copy(alpha = 0.5f),
                        surfaceContainerHigh = Color.Black.copy(alpha = 0.7f),
                        surfaceContainerHighest = Color.Black.copy(alpha = 0.9f)
                    )
                } else {
                    dynamicLightColorScheme(context).copy(
                        surface = Color.White.copy(alpha = 0.3f),
                        surfaceContainer = Color.White.copy(alpha = 0.5f),
                        surfaceContainerHigh = Color.White.copy(alpha = 0.7f),
                        surfaceContainerHighest = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            darkTheme -> LiquidGlassDarkColors
            else -> LiquidGlassLightColors
        }
    }
}

private val LiquidGlassDarkColors = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF6D28D9),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = Color(0xFF06B6D4),
    onSecondary = Color(0xFF003544),
    secondaryContainer = Color(0xFF0891B2),
    onSecondaryContainer = Color(0xFFCFFAFF),
    tertiary = Color(0xFFEC4899),
    onTertiary = Color(0xFF5D1049),
    tertiaryContainer = Color(0xFFDB2777),
    onTertiaryContainer = Color(0xFFFDF2F8),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color.Black.copy(alpha = 0.1f),
    onBackground = Color(0xFFE6E1E5),
    surface = Color.Black.copy(alpha = 0.3f),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color.Black.copy(alpha = 0.4f),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    inverseOnSurface = Color(0xFF1C1B1F),
    inverseSurface = Color(0xFFE6E1E5),
    inversePrimary = Color(0xFF6750A4),
    surfaceTint = Color(0xFF8B5CF6),
    surfaceContainer = Color.Black.copy(alpha = 0.5f),
    surfaceContainerHigh = Color.Black.copy(alpha = 0.7f),
    surfaceContainerHighest = Color.Black.copy(alpha = 0.9f)
)

private val LiquidGlassLightColors = lightColorScheme(
    primary = Color(0xFF6366F1),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF8B5CF6),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF0EA5E9),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF06B6D4),
    onSecondaryContainer = Color(0xFF001F25),
    tertiary = Color(0xFFDB2777),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEC4899),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color.White.copy(alpha = 0.1f),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White.copy(alpha = 0.3f),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color.White.copy(alpha = 0.4f),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    inverseOnSurface = Color(0xFFF4EFF4),
    inverseSurface = Color(0xFF313033),
    inversePrimary = Color(0xFFD0BCFF),
    surfaceTint = Color(0xFF6366F1),
    surfaceContainer = Color.White.copy(alpha = 0.5f),
    surfaceContainerHigh = Color.White.copy(alpha = 0.7f),
    surfaceContainerHighest = Color.White.copy(alpha = 0.9f)
)

/**
 * Apply Liquid Glass theme to the app
 */
@Composable
fun LiquidGlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    enableLiquidGlass: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (enableLiquidGlass) {
        LiquidGlassTheme.LiquidGlassColorScheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor
        )
    } else {
        when {
            dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = {
            if (enableLiquidGlass) {
                LiquidGlassBackground(
                    intensity = 0.6f,
                    speed = 0.3f,
                    enableBlur = true
                ) {
                    content()
                }
            } else {
                content()
            }
        }
    )
}