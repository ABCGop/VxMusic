package com.abcg.music.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.maxrave.domain.model.AppTheme

object ThemeManager {

    // Default Theme
    val Default = AppTheme(
        id = "default",
        name = "Default",
        isPro = false,
        primaryColor = Color(0xFF6200EE).toArgb().toLong(),
        secondaryColor = Color(0xFF03DAC6).toArgb().toLong(),
        backgroundColor = Color(0xFF121212).toArgb().toLong(),
        accentColor = Color(0xFFBB86FC).toArgb().toLong(),
        surfaceColor = Color(0xFF121212).toArgb().toLong(),
        onPrimary = Color.White.toArgb().toLong(),
        onSecondary = Color.Black.toArgb().toLong(),
        onBackground = Color.White.toArgb().toLong()
    )

    // 1. Midnight Purple 🌙
    val MidnightPurple = AppTheme(
        id = "midnight_purple",
        name = "Midnight Purple",
        isPro = true,
        primaryColor = Color(0xFF6B46C1).toArgb().toLong(),
        secondaryColor = Color(0xFFA78BFA).toArgb().toLong(),
        backgroundColor = Color(0xFF1A0B2E).toArgb().toLong(),
        accentColor = Color(0xFFC084FC).toArgb().toLong(),
        surfaceColor = Color(0xFF2D1B4E).toArgb().toLong(),
        onPrimary = Color.White.toArgb().toLong(),
        onSecondary = Color.Black.toArgb().toLong(),
        onBackground = Color.White.toArgb().toLong()
    )

    // 2. Neon Cyberpunk ⚡
    val NeonCyberpunk = AppTheme(
        id = "neon_cyberpunk",
        name = "Neon Cyberpunk",
        isPro = true,
        primaryColor = Color(0xFF00F0FF).toArgb().toLong(),
        secondaryColor = Color(0xFFFF006E).toArgb().toLong(),
        backgroundColor = Color(0xFF0A0A0A).toArgb().toLong(),
        accentColor = Color(0xFF39FF14).toArgb().toLong(),
        surfaceColor = Color(0xFF121212).toArgb().toLong(),
        onPrimary = Color.Black.toArgb().toLong(),
        onSecondary = Color.White.toArgb().toLong(),
        onBackground = Color.White.toArgb().toLong()
    )

    // 3. Rose Gold Luxury 💎
    val RoseGoldLuxury = AppTheme(
        id = "rose_gold_luxury",
        name = "Rose Gold Luxury",
        isPro = true,
        primaryColor = Color(0xFFB76E79).toArgb().toLong(),
        secondaryColor = Color(0xFFF7E7CE).toArgb().toLong(),
        backgroundColor = Color(0xFFFAF9F6).toArgb().toLong(),
        accentColor = Color(0xFF8B4C5C).toArgb().toLong(),
        surfaceColor = Color(0xFFFFFFFF).toArgb().toLong(),
        onPrimary = Color.White.toArgb().toLong(),
        onSecondary = Color.Black.toArgb().toLong(),
        onBackground = Color.Black.toArgb().toLong()
    )

    // 4. Ocean Breeze 🌊
    val OceanBreeze = AppTheme(
        id = "ocean_breeze",
        name = "Ocean Breeze",
        isPro = true,
        primaryColor = Color(0xFF0077BE).toArgb().toLong(),
        secondaryColor = Color(0xFF40E0D0).toArgb().toLong(),
        backgroundColor = Color(0xFF001F3F).toArgb().toLong(),
        accentColor = Color(0xFFFF6B6B).toArgb().toLong(),
        surfaceColor = Color(0xFF003366).toArgb().toLong(),
        onPrimary = Color.White.toArgb().toLong(),
        onSecondary = Color.Black.toArgb().toLong(),
        onBackground = Color.White.toArgb().toLong()
    )

    // 5. Sunset Gradient 🌅
    val SunsetGradient = AppTheme(
        id = "sunset_gradient",
        name = "Sunset Gradient",
        isPro = true,
        primaryColor = Color(0xFFFF6B35).toArgb().toLong(),
        secondaryColor = Color(0xFFFF8CC6).toArgb().toLong(),
        backgroundColor = Color(0xFF2D1B69).toArgb().toLong(),
        accentColor = Color(0xFFFFD93D).toArgb().toLong(),
        surfaceColor = Color(0xFF422A8C).toArgb().toLong(),
        onPrimary = Color.White.toArgb().toLong(),
        onSecondary = Color.Black.toArgb().toLong(),
        onBackground = Color.White.toArgb().toLong()
    )

    // 6. Dark AMOLED Pro 🖤
    val DarkAmoledPro = AppTheme(
        id = "dark_amoled_pro",
        name = "Dark AMOLED Pro",
        isPro = true,
        primaryColor = Color(0xFF000000).toArgb().toLong(),
        secondaryColor = Color(0xFF1C1C1C).toArgb().toLong(),
        backgroundColor = Color(0xFF000000).toArgb().toLong(),
        accentColor = Color(0xFF00D9FF).toArgb().toLong(),
        surfaceColor = Color(0xFF000000).toArgb().toLong(),
        onPrimary = Color.White.toArgb().toLong(),
        onSecondary = Color.White.toArgb().toLong(),
        onBackground = Color.White.toArgb().toLong()
    )

    val themes = listOf(
        Default,
        MidnightPurple,
        NeonCyberpunk,
        RoseGoldLuxury,
        OceanBreeze,
        SunsetGradient,
        DarkAmoledPro
    )

    fun getThemeById(id: String): AppTheme {
        return themes.find { it.id == id } ?: Default
    }
}
