package com.abcg.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.maxrave.common.R

// ============================================
// VxMusic Typography System
// Modern, clean, and readable type scale
// Based on Poppins font family
// ============================================

val fontFamily =
    FontFamily(
        Font(R.font.poppins_medium, FontWeight.Normal, FontStyle.Normal, FontLoadingStrategy.Async),
    )

val typo =
    Typography(
        // ============================================
        // DISPLAY STYLES - Hero sections, large emphasis
        // ============================================
        displayLarge =
            TextStyle(
                fontSize = 57.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = (-0.25).sp,
            ),
        displayMedium =
            TextStyle(
                fontSize = 45.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.sp,
            ),
        displaySmall =
            TextStyle(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.sp,
            ),
        
        // ============================================
        // HEADLINE STYLES - Screen titles, section headers
        // ============================================
        headlineLarge =
            TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.sp,
            ),
        headlineSmall =
            TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.sp,
            ),
        
        // ============================================
        // TITLE STYLES - Card titles, list items, song/album names
        // ============================================
        titleLarge =
            TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.sp,
            ),
        titleMedium =
            TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.15.sp,
            ),
        titleSmall =
            TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.1.sp,
            ),
        
        // ============================================
        // BODY STYLES - Main content, descriptions, artist names
        // ============================================
        bodyLarge =
            TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = fontFamily,
                color = textSecondary,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = fontFamily,
                color = textSecondary,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = fontFamily,
                color = textTertiary,
                letterSpacing = 0.4.sp,
            ),
        
        // ============================================
        // LABEL STYLES - Buttons, tabs, chips, metadata
        // ============================================
        labelLarge =
            TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = fontFamily,
                color = textPrimary,
                letterSpacing = 0.1.sp,
            ),
        labelMedium =
            TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = fontFamily,
                color = textSecondary,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = fontFamily,
                color = textTertiary,
                letterSpacing = 0.5.sp,
            ),
        // ...
    )
