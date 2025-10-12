package com.abcg.music.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Liquid Glass Effect Component (BETA)
 * Creates a fluid, glass-like background effect with animated gradients
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    speed: Float = 1f,
    enableBlur: Boolean = true,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidGlass")
    
    // Animated values for fluid motion
    val animatedOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (8000 / speed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "offset1"
    )
    
    val animatedOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (12000 / speed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "offset2"
    )
    
    val animatedOffset3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (15000 / speed).toInt(),
                easing = LinearEasing
            )
        ),
        label = "offset3"
    )

    Box(modifier = modifier) {
        // Background liquid glass effect
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .let { if (enableBlur) it.blur(20.dp) else it }
        ) {
            drawLiquidGlassEffect(
                size = size,
                offset1 = animatedOffset1,
                offset2 = animatedOffset2,
                offset3 = animatedOffset3,
                intensity = intensity
            )
        }
        
        // Glass overlay with frosted effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        radius = with(density) { 400.dp.toPx() }
                    )
                )
        )
        
        // Content overlay
        content()
    }
}

private fun DrawScope.drawLiquidGlassEffect(
    size: androidx.compose.ui.geometry.Size,
    offset1: Float,
    offset2: Float,
    offset3: Float,
    intensity: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = size.width.coerceAtMost(size.height) / 2
    
    // Create multiple flowing gradient layers
    drawLiquidLayer(
        centerX = centerX + cos(Math.toRadians(offset1.toDouble())).toFloat() * radius * 0.3f,
        centerY = centerY + sin(Math.toRadians(offset1.toDouble())).toFloat() * radius * 0.3f,
        radius = radius * 0.8f,
        colors = listOf(
            Color(0xFF6366F1).copy(alpha = 0.3f * intensity),
            Color(0xFF8B5CF6).copy(alpha = 0.2f * intensity),
            Color(0xFFA855F7).copy(alpha = 0.1f * intensity),
            Color.Transparent
        )
    )
    
    drawLiquidLayer(
        centerX = centerX + cos(Math.toRadians(offset2.toDouble())).toFloat() * radius * 0.4f,
        centerY = centerY + sin(Math.toRadians(offset2.toDouble())).toFloat() * radius * 0.4f,
        radius = radius * 0.6f,
        colors = listOf(
            Color(0xFF06B6D4).copy(alpha = 0.25f * intensity),
            Color(0xFF0EA5E9).copy(alpha = 0.15f * intensity),
            Color(0xFF3B82F6).copy(alpha = 0.1f * intensity),
            Color.Transparent
        )
    )
    
    drawLiquidLayer(
        centerX = centerX + cos(Math.toRadians(offset3.toDouble())).toFloat() * radius * 0.2f,
        centerY = centerY + sin(Math.toRadians(offset3.toDouble())).toFloat() * radius * 0.2f,
        radius = radius * 0.4f,
        colors = listOf(
            Color(0xFFEC4899).copy(alpha = 0.2f * intensity),
            Color(0xFFF472B6).copy(alpha = 0.1f * intensity),
            Color.Transparent
        )
    )
}

private fun DrawScope.drawLiquidLayer(
    centerX: Float,
    centerY: Float,
    radius: Float,
    colors: List<Color>
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = colors,
            center = Offset(centerX, centerY),
            radius = radius
        ),
        radius = radius,
        center = Offset(centerX, centerY),
        blendMode = BlendMode.Screen
    )
}

/**
 * Liquid Glass Card with animated background
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    intensity: Float = 0.8f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
    ) {
        LiquidGlassBackground(
            intensity = intensity,
            speed = 0.5f,
            enableBlur = false
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Liquid Glass Surface for player background
 */
@Composable
fun LiquidGlassPlayerBackground(
    modifier: Modifier = Modifier,
    albumArtColors: List<Color> = emptyList(),
    content: @Composable () -> Unit
) {
    val dominantColor = if (albumArtColors.isNotEmpty()) {
        albumArtColors.first()
    } else {
        MaterialTheme.colorScheme.primary
    }
    
    Box(modifier = modifier) {
        LiquidGlassBackground(
            intensity = 1.2f,
            speed = 0.8f,
            enableBlur = true
        ) {
            // Album art color-influenced overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                dominantColor.copy(alpha = 0.3f),
                                dominantColor.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        content()
    }
}