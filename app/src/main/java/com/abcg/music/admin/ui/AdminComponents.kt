package com.abcg.music.admin.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abcg.music.admin.AppPopup
import com.abcg.music.admin.PopupType

@Composable
fun PopupDialog(
    popup: AppPopup,
    onDismiss: () -> Unit,
    onAction: (String?) -> Unit
) {
    Dialog(onDismissRequest = { 
        if (popup.dismissible) onDismiss() 
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A24)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon based on type
                val (icon, iconColor) = when (popup.type) {
                    PopupType.INFO -> "ℹ️" to Color(0xFF3B82F6)
                    PopupType.WARNING -> "⚠️" to Color(0xFFF59E0B)
                    PopupType.UPDATE -> "🔄" to Color(0xFF10B981)
                    PopupType.PROMO -> "🎉" to Color(0xFF8B5CF6)
                }
                
                // Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Title
                Text(
                    text = popup.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Message
                Text(
                    text = popup.message,
                    fontSize = 15.sp,
                    color = Color(0xFFD1D5DB),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action button
                Button(
                    onClick = { onAction(popup.actionUrl) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF8B5CF6),
                                        Color(0xFF3B82F6)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = popup.buttonText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                
                // Dismiss button (if dismissible)
                if (popup.dismissible) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Maybe Later",
                            color = Color(0xFF9CA3AF),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0F),
                        Color(0xFF1A1A24)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Block icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚫",
                    fontSize = 64.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Account Suspended",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Message
            Text(
                text = "Your account has been temporarily suspended.\nPlease contact support for more information.",
                fontSize = 16.sp,
                color = Color(0xFFD1D5DB),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Support button
            Button(
                onClick = { /* TODO: Open support */ },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFDC2626),
                                    Color(0xFFEF4444)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Contact Support",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .size(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEF4444)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
