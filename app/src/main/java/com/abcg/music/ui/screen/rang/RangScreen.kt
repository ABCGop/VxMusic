package com.abcg.music.ui.screen.rang

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.abcg.music.R
import com.abcg.music.data.repository.ChatPreview
import com.abcg.music.viewModel.RangUiState
import com.abcg.music.viewModel.RangViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangScreen(
    navController: NavController,
    viewModel: RangViewModel = koinViewModel()
) {
    var showAnimation by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(3000)
        showAnimation = false
    }
    
    if (showAnimation) {
        RangAnimationScreen()
    } else {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        
        if (!uiState.isAuthenticated) {
            AuthScreen(viewModel)
        } else {
            RangMainScreen(navController, viewModel, uiState.chats, uiState.isLoading, uiState.error)
        }
    }
}

@Composable
fun RangAnimationScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        RangBackgroundAnimation()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Rang",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF2563EB),
                            Color(0xFFDB2777)
                        )
                    )
                ),
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connect with your vibe",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: RangViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Display ViewModel error or local validation error
    val errorMessage = uiState.error ?: localErrorMessage
    
    Scaffold(
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Welcome to Rang",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF7C3AED),
                            Color(0xFF2563EB),
                            Color(0xFFDB2777)
                        )
                    )
                ),
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                if (isSignUp) "Create your account" else "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Username field (only for sign up)
            if (isSignUp) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.lowercase().replace(" ", "") },
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
            }
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("john@example.com") },
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
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("••••••••") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
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
            
            // Confirm Password field (only for sign up)
            if (isSignUp) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("••••••••") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF7C3AED),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword
                )
                
                if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Text(
                        "Passwords don't match",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Error message
            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sign In/Sign Up Button
            Button(
                onClick = {
                    localErrorMessage = ""
                    
                    if (isSignUp) {
                        // Validation for sign up
                        when {
                            username.isBlank() -> localErrorMessage = "Username is required"
                            username.length < 3 -> localErrorMessage = "Username must be at least 3 characters"
                            email.isBlank() -> localErrorMessage = "Email is required"
                            !email.contains("@") -> localErrorMessage = "Invalid email format"
                            password.isBlank() -> localErrorMessage = "Password is required"
                            password.length < 6 -> localErrorMessage = "Password must be at least 6 characters"
                            confirmPassword.isBlank() -> localErrorMessage = "Please confirm your password"
                            password != confirmPassword -> localErrorMessage = "Passwords don't match"
                            else -> viewModel.signUp(email, password, username)
                        }
                    } else {
                        // Validation for sign in
                        when {
                            email.isBlank() -> localErrorMessage = "Email is required"
                            password.isBlank() -> localErrorMessage = "Password is required"
                            else -> viewModel.signIn(email, password)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        if (isSignUp) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle between sign in and sign up
            TextButton(
                onClick = { 
                    isSignUp = !isSignUp
                    localErrorMessage = ""
                    username = ""
                    email = ""
                    password = ""
                    confirmPassword = ""
                    passwordVisible = false
                    confirmPasswordVisible = false
                }
            ) {
                Text(
                    if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RangMainScreen(
    navController: NavController,
    viewModel: RangViewModel,
    chats: List<ChatPreview>,
    isLoading: Boolean,
    error: String?
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadCurrentProfile()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rang",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7C3AED),
                                    Color(0xFF2563EB),
                                    Color(0xFFDB2777)
                                )
                            )
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        // Navigate to profile screen
                        navController.navigate("profile")
                    }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
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
                                "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, "Search", tint = Color.White)
                    }
                    IconButton(onClick = { showNewChatDialog = true }) {
                        Icon(Icons.Default.Add, "New Chat", tint = Color.White)
                    }
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A)
                )
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF7C3AED)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Error: $error",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadChats() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C3AED)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                chats.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No chats yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start a conversation by tapping the + button",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chats, key = { it.userId }) { chat ->
                            ChatListItem(chat) {
                                navController.navigate("chat/${chat.userId}/${chat.userName}")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Search Dialog
    if (showSearchDialog) {
        SearchUsersDialog(
            onDismiss = { showSearchDialog = false },
            viewModel = viewModel,
            navController = navController
        )
    }
    
    // New Chat Dialog
    if (showNewChatDialog) {
        NewChatDialog(
            onDismiss = { showNewChatDialog = false },
            onSendMessage = { receiverId, message ->
                viewModel.sendMessage(receiverId, message)
                showNewChatDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersDialog(
    onDismiss: () -> Unit,
    viewModel: RangViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 2) {
            kotlinx.coroutines.delay(300) // Debounce
            viewModel.searchUsers(searchQuery)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                "Search Users",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by username or name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, "Clear", tint = Color.White)
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search results
                if (uiState.isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7C3AED))
                    }
                } else if (uiState.searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(uiState.searchResults) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        // Navigate to chat with this user
                                        navController.navigate("chat/${user.id}/${user.username}")
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF2A2A2A)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
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
                                    
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            user.username ?: "Unknown",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (user.full_name != null && user.full_name != user.username) {
                                            Text(
                                                user.full_name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (searchQuery.length >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No users found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Text(
                        "Type at least 2 characters to search",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF7C3AED))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onSendMessage: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = {
            Text(
                "New Chat",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.lowercase().replace(" ", "") },
                    label = { Text("Username") },
                    placeholder = { Text("johndoe") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF7C3AED),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Enter the username of the person you want to message",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && message.isNotBlank()) {
                        onSendMessage(username, message)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED)
                )
            ) {
                Icon(Icons.Rounded.Send, "Send", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}


@Composable
fun ChatListItem(chat: ChatPreview, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A) // Slightly lighter than background
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Online Status
            Box {
                if (chat.userAvatar != null) {
                    AsyncImage(
                        model = chat.userAvatar,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
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
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(2.dp, Color(0xFF1A1A1A), CircleShape)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and Message
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    chat.userName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isLastMessageMine) {
                        val icon = if (chat.lastMessageStatus == "seen") Icons.Rounded.DoneAll else Icons.Rounded.Check
                        val tint = if (chat.lastMessageStatus == "seen") Color(0xFF34B7F1) else Color.White.copy(alpha = 0.6f)
                        Icon(
                            icon,
                            contentDescription = "Status",
                            tint = tint,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                    }
                    Text(
                        chat.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Time and Unread Badge
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    chat.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (chat.unreadCount > 0) Color(0xFF7C3AED) else Color.White.copy(alpha = 0.4f),
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
                
                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7C3AED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RangBackgroundAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "RangAnimation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        drawOrb(width * 0.2f, height * 0.3f, 300f, Color(0xFF7C3AED), time, 0f)
        drawOrb(width * 0.8f, height * 0.7f, 400f, Color(0xFF2563EB), time, 2f)
        drawOrb(width * 0.5f, height * 0.5f, 350f, Color(0xFFDB2777), time, 4f)
    }
}

fun DrawScope.drawOrb(x: Float, y: Float, radius: Float, color: Color, time: Float, offset: Float) {
    val moveX = cos(time + offset) * 100f
    val moveY = sin(time + offset) * 100f
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(x + moveX, y + moveY),
            radius = radius
        ),
        center = Offset(x + moveX, y + moveY),
        radius = radius
    )
}
