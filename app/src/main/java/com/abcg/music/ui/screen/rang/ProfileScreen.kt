package com.abcg.music.ui.screen.rang

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.abcg.music.viewModel.RangViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userId: String? = null,
    viewModel: RangViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    
    // Determine if we are viewing our own profile
    val isMyProfile = remember(userId, uiState.currentProfile) {
        userId == null || userId == uiState.currentProfile?.id
    }
    
    // Load profile if viewing someone else
    LaunchedEffect(userId) {
        if (userId != null && !isMyProfile) {
            viewModel.loadUserProfile(userId)
        }
    }
    
    val profileToDisplay = if (isMyProfile) uiState.currentProfile else uiState.viewedProfile
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isMyProfile) "My Profile" else "Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A)
                )
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        if (uiState.isLoading && profileToDisplay == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF7C3AED))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED),
                                    Color(0xFF2563EB),
                                    Color(0xFFDB2777)
                                )
                            )
                        )
                        .clickable(enabled = isMyProfile) { showImagePickerDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Load actual image if available
                     if (profileToDisplay?.avatar_url != null) {
                        // Placeholder for AsyncImage
                         Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    
                    // Edit badge (only for my profile)
                    if (isMyProfile) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF7C3AED)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tap to change photo hint (only for my profile)
                if (isMyProfile) {
                    Text(
                        "Tap to change photo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Username
                Text(
                    profileToDisplay?.username ?: "Unknown",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Full Name
                Text(
                    profileToDisplay?.full_name ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (isMyProfile) {
                    // Edit Profile Button
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Edit Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Action Buttons for Other User
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Message Button
                        Button(
                            onClick = { 
                                // Navigate to chat
                                if (profileToDisplay != null) {
                                    navController.navigate("chat/${profileToDisplay.id}/${profileToDisplay.username}")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C3AED)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Message")
                        }
                        
                        // Block Button
                        OutlinedButton(
                            onClick = { 
                                if (userId != null) viewModel.blockUser(userId)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Block")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Profile Info Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ProfileInfoRow("Username", profileToDisplay?.username ?: "N/A")
                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        )
                        ProfileInfoRow("Full Name", profileToDisplay?.full_name ?: "N/A")
                        
                        // Add more info if available (e.g. Bio, Joined Date)
                        if (profileToDisplay?.bio != null) {
                             Divider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            )
                            ProfileInfoRow("About", profileToDisplay.bio)
                        }
                    }
                }
            }
        }
    }
    
    // Image Picker Dialog
    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            onImageSelected = { imageUrl ->
                // For now, just show a message that image upload is coming soon
                showImagePickerDialog = false
            }
        )
    }
    
    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            currentUsername = uiState.currentProfile?.username ?: "",
            currentFullName = uiState.currentProfile?.full_name ?: "",
            onDismiss = { showEditDialog = false },
            onSave = { username, fullName ->
                viewModel.updateProfile(username, fullName)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentFullName: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var username by remember { mutableStateOf(currentUsername) }
    var fullName by remember { mutableStateOf(currentFullName) }
    var errorMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                "Edit Profile",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it.lowercase().replace(" ", "")
                        errorMessage = ""
                    },
                    label = { Text("Username") },
                    placeholder = { Text("johndoe") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF7C3AED),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { 
                        fullName = it
                        errorMessage = ""
                    },
                    label = { Text("Full Name") },
                    placeholder = { Text("John Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF7C3AED),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    singleLine = true
                )
                
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        errorMessage,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        username.isBlank() -> errorMessage = "Username cannot be empty"
                        username.length < 3 -> errorMessage = "Username must be at least 3 characters"
                        fullName.isBlank() -> errorMessage = "Full name cannot be empty"
                        else -> onSave(username, fullName)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED)
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                "Change Profile Picture",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Profile picture upload is coming soon!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                Text(
                    "For now, you can:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "• Edit your username and full name\n• Enjoy the gradient avatar\n• Stay tuned for image uploads!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!", color = Color(0xFF7C3AED))
            }
        }
    )
}
