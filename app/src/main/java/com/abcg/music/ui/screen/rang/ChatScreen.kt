package com.abcg.music.ui.screen.rang

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.abcg.music.data.repository.Message
import com.abcg.music.viewModel.ChatViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    otherUserId: String,
    otherUsername: String,
    viewModel: ChatViewModel = koinViewModel { parametersOf(otherUserId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            // Cleanup if needed
        }
    }
    
    // Image Picker
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (bytes != null) {
                viewModel.sendImageMessage(bytes)
            }
        }
    }
    
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            navController.navigate("profile?userId=$otherUserId")
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF7C3AED),
                                            Color(0xFF2563EB)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                otherUsername,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (uiState.isOtherUserTyping) "Typing..." 
                                else if (uiState.isOtherUserOnline) "Online"
                                else "Offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Video Call */ }) {
                        // Icon(Icons.Default.Videocam, "Video Call", tint = Color.White)
                    }
                    IconButton(onClick = { /* Voice Call */ }) {
                        // Icon(Icons.Default.Call, "Call", tint = Color.White)
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A)
                )
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0A0A0A))
        ) {
            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (uiState.isLoading && uiState.messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF7C3AED))
                        }
                    }
                } else {
                    items(uiState.messages) { message ->
                        MessageBubble(
                            message = message,
                            isCurrentUser = message.sender_id == uiState.currentUserId
                        )
                    }
                }
            }
            
            // Error message display
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                // Clear error - you'll need to add this function to ViewModel
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                "Dismiss",
                                tint = Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Message Input
            ChatInputBar(
                messageText = messageText,
                onMessageChange = {
                    messageText = it
                    viewModel.onTyping()
                },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText.trim())
                        messageText = ""
                    }
                },
                onAttach = { 
                    imagePickerLauncher.launch("image/*")
                }
            )
        }
    }
}

@Composable
fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1F2C34)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = { /* Emoji */ }) {
                    // Icon(Icons.Default.EmojiEmotions, "Emoji", tint = Color.Gray)
                }
                
                TextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message", color = Color.Gray) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF00A884)
                    ),
                    maxLines = 6
                )
                
                IconButton(onClick = onAttach) {
                    Icon(Icons.Default.AttachFile, "Attach", tint = Color.Gray)
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (messageText.isNotBlank()) {
            FloatingActionButton(
                onClick = onSend,
                containerColor = Color(0xFF00A884),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean
) {
    val bubbleColor = if (isCurrentUser) Color(0xFF005C4B) else Color(0xFF1F2C34)
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 12.dp
            ),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Content
                when (message.type) {
                    "image" -> {
                        // Display Image using Coil
                        if (message.media_url != null) {
                            var showImageDialog by remember { mutableStateOf(false) }
                            val context = LocalContext.current
                            
                            Column {
                                AsyncImage(
                                    model = message.media_url,
                                    contentDescription = "Image Message",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black.copy(alpha = 0.2f))
                                        .clickable { showImageDialog = true },
                                    contentScale = ContentScale.Fit
                                )
                                
                                // Image options bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Share button
                                    IconButton(
                                        onClick = {
                                            shareImage(context, message.media_url)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            "Share Image",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // View full button
                                    IconButton(
                                        onClick = {
                                            viewImageInBrowser(context, message.media_url)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Download,
                                            "View Full",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Full image dialog on click
                            if (showImageDialog) {
                                AlertDialog(
                                    onDismissRequest = { showImageDialog = false },
                                    confirmButton = {
                                        TextButton(onClick = { showImageDialog = false }) {
                                            Text("Close")
                                        }
                                    },
                                    text = {
                                        AsyncImage(
                                            model = message.media_url,
                                            contentDescription = "Full Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 600.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Image Loading...", color = Color.White)
                            }
                        }
                    }
                    else -> {
                        Text(
                            message.content ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                
                // Timestamp & Status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatTimestamp(message.created_at ?: ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.status == "seen") Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = "Status",
                            tint = if (message.status == "seen") Color(0xFF53BDEB) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val parts = timestamp.split("T")
        if (parts.size > 1) {
            val timePart = parts[1].split(".")[0]
            val hourMin = timePart.substring(0, 5)
            hourMin
        } else {
            "Now"
        }
    } catch (e: Exception) {
        "Now"
    }
}

// Helper function to share image
private fun shareImage(context: Context, imageUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this image: $imageUrl")
        }
        context.startActivity(Intent.createChooser(intent, "Share Image"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Helper function to view/download image in browser
private fun viewImageInBrowser(context: Context, imageUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(imageUrl)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
