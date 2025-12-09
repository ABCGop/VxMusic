package com.abcg.music.sdui.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.abcg.music.sdui.*

@Composable
fun DynamicUiRenderer(
    sections: List<DynamicSection>,
    modifier: Modifier = Modifier,
    navController: androidx.navigation.NavController? = null
) {
    Column(modifier = modifier) {
        sections.forEach { section ->
            DynamicSectionItem(section, navController)
        }
    }
}

@Composable
fun DynamicSectionItem(section: DynamicSection, navController: androidx.navigation.NavController? = null) {
    val context = LocalContext.current
    
    val modifier = Modifier
        .fillMaxWidth()
        .padding(section.style?.padding?.dp ?: 0.dp)
        .then(
            if (section.style?.backgroundColor != null) {
                Modifier.background(
                    Color(android.graphics.Color.parseColor(section.style.backgroundColor)),
                    RoundedCornerShape(section.style.cornerRadius?.dp ?: 0.dp)
                )
            } else Modifier
        )
        .then(
            if (section.action != null) {
                Modifier.clickable {
                    handleAction(context, section.action, navController)
                }
            } else Modifier
        )

    when (section.type) {
        SectionType.BANNER -> {
            Card(
                modifier = modifier,
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box {
                    if (section.content.imageUrl != null) {
                        AsyncImage(
                            model = section.content.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (section.content.title != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = section.content.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (section.content.subtitle != null) {
                                Text(
                                    text = section.content.subtitle,
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        
        SectionType.CARD_LIST -> {
            Column(modifier = modifier) {
                if (section.content.title != null) {
                    Text(
                         text = section.content.title,
                         color = Color.White,
                         fontWeight = FontWeight.Bold,
                         fontSize = 18.sp,
                         modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                    )
                }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(section.content.items ?: emptyList()) { item ->
                        DynamicSectionItem(item, navController)
                    }
                }
            }
        }
        
        SectionType.TEXT_BLOCK -> {
             Column(modifier = modifier.padding(8.dp)) {
                 if (section.content.title != null) {
                     Text(
                         text = section.content.title,
                         color = section.style?.textColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: Color.White,
                         fontWeight = FontWeight.Bold,
                         fontSize = 16.sp
                     )
                 }
                 if (section.content.subtitle != null) {
                     Text(
                         text = section.content.subtitle,
                         color = Color.LightGray,
                         fontSize = 14.sp
                     )
                 }
             }
        }
        
        SectionType.BUTTON -> {
            Button(
                onClick = {
                    if (section.action != null) handleAction(context, section.action, navController)
                },
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = section.style?.backgroundColor?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = section.content.title ?: "Button")
            }
        }
        
        SectionType.SPACER -> {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

fun handleAction(context: android.content.Context, action: DynamicAction, navController: androidx.navigation.NavController? = null) {
    when (action.type) {
        ActionType.OPEN_URL -> {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.value))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
            }
        }
        ActionType.SHOW_TOAST -> {
            Toast.makeText(context, action.value, Toast.LENGTH_SHORT).show()
        }
        ActionType.NAVIGATE -> {
             if (navController != null) {
                 if (action.value == "wrapped") {
                     navController.navigate(com.abcg.music.ui.navigation.destination.home.WrappedDestination)
                 } else {
                     Toast.makeText(context, "Unknown route: ${action.value}", Toast.LENGTH_SHORT).show()
                 }
             } else {
                 Toast.makeText(context, "Navigation not available", Toast.LENGTH_SHORT).show()
             }
        }
        ActionType.SHOW_DIALOG -> {
             Toast.makeText(context, "Alert: ${action.value}", Toast.LENGTH_LONG).show()
        }
    }
}
