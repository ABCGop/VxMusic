package com.abcg.music.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abcg.music.data.repository.AuthRepository
import com.abcg.music.data.repository.ChatRepository
import com.abcg.music.data.repository.Message
import com.abcg.music.data.repository.SupabaseStorageRepository
import com.maxrave.logger.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val currentUserId: String = "",
    val chatId: String? = null,
    val error: String? = null,
    val isOtherUserTyping: Boolean = false,
    val isOtherUserOnline: Boolean = false
)

class ChatViewModel(
    private val otherUserId: String,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val storageRepository: SupabaseStorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(currentUserId = currentUser.id)
            initializeChat()
            subscribeToUserStatus()
        }
    }

    private fun initializeChat() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val currentUser = authRepository.getCurrentUser() ?: return@launch
            
            chatRepository.createOrGetChat(currentUser.id, otherUserId).fold(
                onSuccess = { chatId ->
                    _uiState.value = _uiState.value.copy(chatId = chatId)
                    // Load messages first, then subscribe
                    loadMessagesAndSubscribe(chatId)
                    subscribeToTyping(chatId)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to initialize chat",
                        isLoading = false
                    )
                }
            )
        }
    }

    private fun loadMessagesAndSubscribe(chatId: String) {
        viewModelScope.launch {
            android.util.Log.d("ChatViewModel", "=== LOADING MESSAGES FROM SUPABASE ===")
            android.util.Log.d("ChatViewModel", "ChatId: $chatId")
            Logger.d("ChatViewModel", "Loading messages for chatId: $chatId")
            chatRepository.getMessages(chatId).fold(
                onSuccess = { messages ->
                    android.util.Log.d("ChatViewModel", "✓ Loaded ${messages.size} messages from Supabase")
                    Logger.d("ChatViewModel", "Loaded ${messages.size} messages")
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                    // Start Supabase real-time subscription
                    android.util.Log.d("ChatViewModel", "Starting Supabase real-time subscription...")
                    subscribeToMessages(chatId)
                },
                onFailure = { error ->
                    android.util.Log.e("ChatViewModel", "✗ Failed to load messages: ${error.message}")
                    Logger.e("ChatViewModel", "Failed to load messages", error)
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to load messages",
                        isLoading = false
                    )
                }
            )
        }
    }

    private var subscriptionJob: kotlinx.coroutines.Job? = null

    private fun subscribeToMessages(chatId: String) {
        // Cancel existing subscription to prevent duplicates
        subscriptionJob?.cancel()
        
        subscriptionJob = viewModelScope.launch {
            Logger.d("ChatViewModel", "Subscribing to Supabase messages for chatId: $chatId")
            try {
                chatRepository.subscribeToMessages(chatId).collect { message ->
                    Logger.d("ChatViewModel", "Received new message: id=${message.id}, type=${message.type}, content=${message.content?.take(50)}")
                    val currentMessages = _uiState.value.messages.toMutableList()
                    
                    // Check if this is replacing an optimistic message (id is null)
                    val optimisticIndex = currentMessages.indexOfFirst { 
                        it.id == null && 
                        it.sender_id == message.sender_id && 
                        (it.content == message.content || (it.content.isNullOrEmpty() && message.content.isNullOrEmpty()))
                        // Removed type check strictness and timestamp check to be more robust
                    }
                    
                    if (optimisticIndex != -1) {
                        // Replace optimistic message with real one
                        currentMessages[optimisticIndex] = message
                        Logger.d("ChatViewModel", "Replaced optimistic message with real message")
                    } else {
                         // Check for duplicates by ID
                         val exists = currentMessages.any { it.id != null && it.id == message.id }
                         if (!exists) {
                             currentMessages.add(message)
                             Logger.d("ChatViewModel", "Message added to list. Total messages: ${currentMessages.size}")
                         } else {
                             Logger.d("ChatViewModel", "Message already exists in list, skipping")
                             return@collect // Don't update UI if duplicate
                         }
                    }
                    
                    // Always update the UI state to trigger recomposition
                    _uiState.value = _uiState.value.copy(
                        messages = currentMessages,
                        error = null // Clear any previous errors
                    )
                }
            } catch (e: Exception) {
                Logger.e("ChatViewModel", "Error in message subscription", e)
                _uiState.value = _uiState.value.copy(
                    error = "Real-time updates failed: ${e.message}"
                )
            }
        }
    }

    fun sendMessage(content: String) {
        val chatId = _uiState.value.chatId
        if (chatId == null) {
            _uiState.value = _uiState.value.copy(
                error = "Chat not initialized. Please try reopening the chat."
            )
            return
        }
        
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                error = "User not authenticated"
            )
            return
        }

        // Optimistic update - show message immediately
        val tempTimestamp = System.currentTimeMillis().toString()
        val optimisticMessage = Message(
            id = null,
            created_at = tempTimestamp,
            sender_id = currentUser.id,
            receiver_id = otherUserId,
            chat_id = chatId,
            content = content,
            type = "text",
            reply_to_id = null,
            is_forwarded = false,
            status = "sending",
            media_url = null,
            media_duration = null
        )
        
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(optimisticMessage)
        _uiState.value = _uiState.value.copy(messages = currentMessages)

        viewModelScope.launch {
            android.util.Log.d("ChatViewModel", "Sending message to Supabase...")
            chatRepository.sendTextMessage(
                chatId = chatId,
                content = content
            ).fold(
                onSuccess = { messageId ->
                    android.util.Log.d("ChatViewModel", "✓ Message sent to Supabase: $messageId")
                    // Update optimistic message status
                    val updatedMessages = _uiState.value.messages.toMutableList()
                    val tempIndex = updatedMessages.indexOfFirst { it.created_at == tempTimestamp && it.id == null }
                    if (tempIndex != -1) {
                        updatedMessages[tempIndex] = updatedMessages[tempIndex].copy(status = "sent")
                        _uiState.value = _uiState.value.copy(messages = updatedMessages)
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("ChatViewModel", "✗ Failed to send message: ${error.message}")
                    // Remove temp message on failure
                    val updatedMessages = _uiState.value.messages.toMutableList()
                    updatedMessages.removeAll { it.created_at == tempTimestamp && it.id == null }
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        error = error.message ?: "Failed to send message"
                    )
                }
            )
        }
    }
    
    fun sendImageMessage(byteArray: ByteArray) {
        val chatId = _uiState.value.chatId
        if (chatId == null) {
            _uiState.value = _uiState.value.copy(
                error = "Chat not initialized. Please try reopening the chat."
            )
            return
        }
        
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                error = "User not authenticated"
            )
            return
        }
        
        // Optimistic update - show image message immediately
        val tempTimestamp = System.currentTimeMillis().toString()
        val optimisticMessage = Message(
            id = null,
            created_at = tempTimestamp,
            sender_id = currentUser.id,
            receiver_id = otherUserId,
            chat_id = chatId,
            content = "",
            type = "image",
            reply_to_id = null,
            is_forwarded = false,
            status = "sending",
            media_url = null, // Will be updated once uploaded
            media_duration = null
        )
        
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(optimisticMessage)
        _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = true)

        viewModelScope.launch {
            // 1. Upload image
            storageRepository.uploadChatMedia(chatId, byteArray, "jpg").fold(
                onSuccess = { url ->
                    // Update optimistic message with URL
                    val updatedMessages = _uiState.value.messages.toMutableList()
                    val tempIndex = updatedMessages.indexOfFirst { it.created_at == tempTimestamp && it.id == null }
                    if (tempIndex != -1) {
                        updatedMessages[tempIndex] = updatedMessages[tempIndex].copy(media_url = url)
                        _uiState.value = _uiState.value.copy(messages = updatedMessages)
                    }
                    
                    // 2. Send message with image URL to Supabase
                    android.util.Log.d("ChatViewModel", "Sending image message to Supabase with URL: $url")
                    chatRepository.sendImageMessage(
                        chatId = chatId,
                        imageUrl = url
                    ).fold(
                        onSuccess = { messageId ->
                            android.util.Log.d("ChatViewModel", "✓ Image message sent to Supabase: $messageId")
                            // Update optimistic message status
                            val finalMessages = _uiState.value.messages.toMutableList()
                            val finalIndex = finalMessages.indexOfFirst { it.created_at == tempTimestamp && it.id == null }
                            if (finalIndex != -1) {
                                finalMessages[finalIndex] = finalMessages[finalIndex].copy(status = "sent")
                            }
                            _uiState.value = _uiState.value.copy(messages = finalMessages, isLoading = false)
                        },
                        onFailure = { error ->
                            // Remove temp message on failure
                            val failedMessages = _uiState.value.messages.toMutableList()
                            failedMessages.removeAll { it.created_at == tempTimestamp && it.id == null }
                            _uiState.value = _uiState.value.copy(
                                messages = failedMessages,
                                error = error.message ?: "Failed to send image message",
                                isLoading = false
                            )
                        }
                    )
                },
                onFailure = { error ->
                    // Remove temp message on upload failure
                    val failedMessages = _uiState.value.messages.toMutableList()
                    failedMessages.removeAll { it.created_at == tempTimestamp && it.id == null }
                    _uiState.value = _uiState.value.copy(
                        messages = failedMessages,
                        error = error.message ?: "Failed to upload image",
                        isLoading = false
                    )
                }
            )
        }
    }
    
    fun sendAudioMessage(byteArray: ByteArray, duration: Int) {
        val chatId = _uiState.value.chatId
        if (chatId == null) {
            _uiState.value = _uiState.value.copy(
                error = "Chat not initialized. Please try reopening the chat."
            )
            return
        }
        
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                error = "User not authenticated"
            )
            return
        }
        
        // Optimistic update - show audio message immediately
        val tempTimestamp = System.currentTimeMillis().toString()
        val optimisticMessage = Message(
            id = null,
            created_at = tempTimestamp,
            sender_id = currentUser.id,
            receiver_id = otherUserId,
            chat_id = chatId,
            content = "",
            type = "audio",
            reply_to_id = null,
            is_forwarded = false,
            status = "sending",
            media_url = null, // Will be updated once uploaded
            media_duration = duration
        )
        
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(optimisticMessage)
        _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = true)

        viewModelScope.launch {
            // 1. Upload audio
            storageRepository.uploadVoiceNote(chatId, byteArray).fold(
                onSuccess = { url ->
                    // Update optimistic message with URL
                    val updatedMessages = _uiState.value.messages.toMutableList()
                    val tempIndex = updatedMessages.indexOfFirst { it.created_at == tempTimestamp && it.id == null }
                    if (tempIndex != -1) {
                        updatedMessages[tempIndex] = updatedMessages[tempIndex].copy(media_url = url)
                        _uiState.value = _uiState.value.copy(messages = updatedMessages)
                    }
                    
                    // 2. Send message with audio URL
                    chatRepository.sendMessage(
                        senderId = currentUser.id,
                        chatId = chatId,
                        content = null,
                        type = "audio",
                        mediaUrl = url,
                        mediaDuration = duration
                    ).fold(
                        onSuccess = {
                            // Update optimistic message status - real-time will replace with actual
                            val finalMessages = _uiState.value.messages.toMutableList()
                            val finalIndex = finalMessages.indexOfFirst { it.created_at == tempTimestamp && it.id == null }
                            if (finalIndex != -1) {
                                finalMessages[finalIndex] = finalMessages[finalIndex].copy(status = "sent")
                            }
                            _uiState.value = _uiState.value.copy(messages = finalMessages, isLoading = false)
                        },
                        onFailure = { error ->
                            // Remove temp message on failure
                            val failedMessages = _uiState.value.messages.toMutableList()
                            failedMessages.removeAll { it.created_at == tempTimestamp && it.id == null }
                            _uiState.value = _uiState.value.copy(
                                messages = failedMessages,
                                error = error.message ?: "Failed to send voice message",
                                isLoading = false
                            )
                        }
                    )
                },
                onFailure = { error ->
                    // Remove temp message on upload failure
                    val failedMessages = _uiState.value.messages.toMutableList()
                    failedMessages.removeAll { it.created_at == tempTimestamp && it.id == null }
                    _uiState.value = _uiState.value.copy(
                        messages = failedMessages,
                        error = error.message ?: "Failed to upload voice note",
                        isLoading = false
                    )
                }
            )
        }
    }
    
    // --- Real-time Indicators ---
    
    private var typingJob: kotlinx.coroutines.Job? = null
    
    fun onTyping() {
        val chatId = _uiState.value.chatId ?: return
        
        viewModelScope.launch {
            chatRepository.sendTyping(chatId, true)
            
            typingJob?.cancel()
            typingJob = launch {
                kotlinx.coroutines.delay(3000)
                chatRepository.sendTyping(chatId, false)
            }
        }
    }
    
    private fun subscribeToTyping(chatId: String) {
        viewModelScope.launch {
            chatRepository.subscribeToTyping(chatId).collect { isTyping ->
                _uiState.value = _uiState.value.copy(isOtherUserTyping = isTyping)
            }
        }
    }
    
    private fun subscribeToUserStatus() {
        viewModelScope.launch {
            chatRepository.subscribeToUserStatus(otherUserId).collect { profile ->
                _uiState.value = _uiState.value.copy(isOtherUserOnline = profile.is_online)
            }
        }
    }
}
