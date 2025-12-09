package com.abcg.music.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    @SerialName("full_name")
    val full_name: String? = null,
    @SerialName("avatar_url")
    val avatar_url: String? = null,
    val bio: String? = null,
    @SerialName("last_seen")
    val last_seen: String? = null,
    @SerialName("is_online")
    val is_online: Boolean = false,
    @SerialName("updated_at")
    val updated_at: String? = null
)

@Serializable
data class Chat(
    val id: String,
    @SerialName("created_at")
    val created_at: String? = null,
    @SerialName("is_group")
    val is_group: Boolean = false,
    val name: String? = null,
    @SerialName("image_url")
    val image_url: String? = null
)

@Serializable
data class ChatParticipant(
    @SerialName("chat_id")
    val chat_id: String,
    @SerialName("user_id")
    val user_id: String,
    @SerialName("joined_at")
    val joined_at: String? = null
)

@Serializable
data class Message(
    val id: Long? = null,
    @SerialName("created_at")
    val created_at: String? = null,
    @SerialName("sender_id")
    val sender_id: String,
    @SerialName("receiver_id")
    val receiver_id: String? = null,
    @SerialName("chat_id")
    val chat_id: String? = null,
    val content: String? = null,
    val type: String = "text",
    @SerialName("reply_to_id")
    val reply_to_id: Long? = null,
    @SerialName("is_forwarded")
    val is_forwarded: Boolean = false,
    val status: String = "sent",
    @SerialName("media_url")
    val media_url: String? = null,
    @SerialName("media_duration")
    val media_duration: Int? = null,
    @SerialName("edited_at")
    val edited_at: String? = null,
    @SerialName("seen_at")
    val seen_at: String? = null,
    @SerialName("is_deleted")
    val is_deleted: Boolean = false,
    @SerialName("deleted_for")
    val deleted_for: List<String>? = null
)

@Serializable
data class Follow(
    val follower_id: String,
    val following_id: String,
    val created_at: String? = null
)

data class ChatPreview(
    val chatId: String,
    val userId: String, // The other user's ID (for 1-on-1)
    val userName: String,
    val userAvatar: String?,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val type: String = "text",
    val isLastMessageMine: Boolean = false,
    val lastMessageStatus: String = "sent"
)

class ChatRepository(
    private val supabase: SupabaseClient
) {
    // --- Profile Operations ---

    suspend fun getProfile(userId: String): Result<Profile> = try {
        val profile = supabase.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<Profile>()
        Result.success(profile)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getProfileByUsername(username: String): Result<Profile> = try {
        val profiles = supabase.from("profiles")
            .select { filter { eq("username", username) } }
            .decodeList<Profile>()
        
        if (profiles.isEmpty()) {
            Result.failure(Exception("User with username '$username' not found"))
        } else {
            Result.success(profiles.first())
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateProfile(profile: Profile): Result<Unit> = try {
        supabase.from("profiles")
            .update(profile) { filter { eq("id", profile.id) } }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchUsers(query: String): Result<List<Profile>> = try {
        val users = supabase.from("profiles")
            .select {
                filter {
                    or {
                        ilike("username", "%$query%")
                        ilike("full_name", "%$query%")
                    }
                }
            }
            .decodeList<Profile>()
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- Chat Operations ---

    // Create or get existing 1-on-1 chat
    suspend fun createOrGetChat(currentUserId: String, otherUserId: String): Result<String> {
        return try {
            // 1. Check if a common chat already exists using JSON parsing
        val myChatsJson = supabase.from("chat_participants")
            .select() {
                filter { eq("user_id", currentUserId) }
            }
            .decodeList<kotlinx.serialization.json.JsonObject>()
            
        val myChats = myChatsJson.mapNotNull { 
            it["chat_id"]?.toString()?.removeSurrounding("\"") 
        }

            if (myChats.isNotEmpty()) {
                val commonChatsJson = supabase.from("chat_participants")
                .select() {
                    filter {
                        isIn("chat_id", myChats)
                        eq("user_id", otherUserId)
                    }
                }
                .decodeList<kotlinx.serialization.json.JsonObject>()
            
            if (commonChatsJson.isNotEmpty()) {
                val chatId = commonChatsJson.first()["chat_id"]?.toString()?.removeSurrounding("\"") ?: ""
                    val chat = supabase.from("chats")
                        .select { filter { eq("id", chatId) } }
                        .decodeSingleOrNull<Chat>()
                    
                    if (chat != null && !chat.is_group) {
                        return Result.success(chatId)
                    }
                }
            }

            // 2. If not found, create new chat
            val newChat = supabase.from("chats")
                .insert(Chat(id = java.util.UUID.randomUUID().toString())) {
                    select()
                }
                .decodeSingle<Chat>()

            // 3. Add participants
            val participants = listOf(
                ChatParticipant(chat_id = newChat.id, user_id = currentUserId),
                ChatParticipant(chat_id = newChat.id, user_id = otherUserId)
            )
            supabase.from("chat_participants").insert(participants)

            Result.success(newChat.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get recent chats list
    suspend fun getRecentChats(userId: String): Result<List<ChatPreview>> {
        return try {
        // 1. Get all chats user is participating in using raw JSON to avoid serialization issues
        val myChatsJson = supabase.from("chat_participants")
            .select() {
                filter { eq("user_id", userId) }
            }
            .decodeList<kotlinx.serialization.json.JsonObject>()
            
        val chatIds = myChatsJson.mapNotNull { 
            it["chat_id"]?.toString()?.removeSurrounding("\"") 
        }
        
        if (chatIds.isEmpty()) return Result.success(emptyList())

        // 2. Get chat details and other participants
        val previews = mutableListOf<ChatPreview>()
        
        for (chatId in chatIds) {
            // Get other participant using raw JSON
            val otherParticipantsJson = supabase.from("chat_participants")
                .select() {
                    filter { 
                        eq("chat_id", chatId)
                        neq("user_id", userId)
                    }
                }
                .decodeList<kotlinx.serialization.json.JsonObject>()
            
            if (otherParticipantsJson.isNotEmpty()) {
                val otherUserId = otherParticipantsJson.first()["user_id"]?.toString()?.removeSurrounding("\"") ?: continue
                val otherProfile = getProfile(otherUserId).getOrNull()
                
                // Get last message
                val lastMessages = supabase.from("messages")
                    .select {
                        filter { eq("chat_id", chatId) }
                        order("created_at", Order.DESCENDING)
                        limit(1)
                    }
                    .decodeList<Message>()
                
                // Always add chat preview, even if no messages
                if (lastMessages.isNotEmpty()) {
                    val lastMsg = lastMessages.first()
                    val displayText = when(lastMsg.type) {
                        "image" -> "ðŸ“· Image"
                        "audio" -> "ðŸŽ¤ Voice Message"
                        else -> lastMsg.content ?: ""
                    }
                    
                    previews.add(
                        ChatPreview(
                            chatId = chatId,
                            userId = otherUserId,
                            userName = otherProfile?.username ?: otherProfile?.full_name ?: "Unknown",
                            userAvatar = otherProfile?.avatar_url,
                            lastMessage = displayText,
                            timestamp = formatTimestamp(lastMsg.created_at ?: ""),
                            type = lastMsg.type,
                            isLastMessageMine = lastMsg.sender_id == userId,
                            lastMessageStatus = lastMsg.status,
                            isOnline = otherProfile?.is_online ?: false
                        )
                    )
                } else {
                    // Chat exists but no messages yet
                    previews.add(
                        ChatPreview(
                            chatId = chatId,
                            userId = otherUserId,
                            userName = otherProfile?.username ?: otherProfile?.full_name ?: "Unknown",
                            userAvatar = otherProfile?.avatar_url,
                            lastMessage = "No messages yet",
                            timestamp = "Now",
                            type = "text",
                            isLastMessageMine = false,
                            lastMessageStatus = "sent",
                            isOnline = otherProfile?.is_online ?: false
                        )
                    )
                }
            }
        }
        
            Result.success(previews.sortedByDescending { it.timestamp }) // Note: timestamp sorting needs proper parsing
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Message Operations ---

    suspend fun sendMessage(
        senderId: String, 
        chatId: String, 
        content: String?, 
        type: String = "text",
        mediaUrl: String? = null,
        mediaDuration: Int? = null
    ): Result<Unit> = try {
        val message = Message(
            sender_id = senderId,
            chat_id = chatId,
            content = content ?: "", // Empty string instead of null to satisfy database constraint
            type = type,
            media_url = mediaUrl,
            media_duration = mediaDuration,
            status = "sent"
        )
        supabase.from("messages").insert(message)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Legacy support for username based sending (creates chat if needed)
    suspend fun sendMessageByUsername(senderId: String, receiverUsername: String, content: String): Result<Unit> = try {
        val receiverProfile = getProfileByUsername(receiverUsername).getOrThrow()
        val chatId = createOrGetChat(senderId, receiverProfile.id).getOrThrow()
        sendMessage(senderId, chatId, content)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    // Wrapper functions for ChatViewModel compatibility
    suspend fun sendTextMessage(chatId: String, content: String): Result<Long> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            sendMessage(currentUser.id, chatId, content, "text")
            // Return a dummy ID since sendMessage returns Unit
            Result.success(System.currentTimeMillis())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendImageMessage(chatId: String, imageUrl: String): Result<Long> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            sendMessage(currentUser.id, chatId, null, "image", imageUrl)
            // Return a dummy ID since sendMessage returns Unit
            Result.success(System.currentTimeMillis())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Alias for ChatViewModel compatibility
    fun subscribeToMessages(chatId: String): Flow<Message> = subscribeToChatMessages(chatId)

    suspend fun getMessages(chatId: String): Result<List<Message>> = try {
        val messages = supabase.from("messages")
            .select {
                filter { eq("chat_id", chatId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<Message>()
        Result.success(messages)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Real-time message updates for a specific chat
    fun subscribeToChatMessages(chatId: String): Flow<Message> = callbackFlow {
        android.util.Log.d("ChatRepository", "=== REALTIME SUBSCRIPTION START ===")
        android.util.Log.d("ChatRepository", "Subscribing to chatId: $chatId")
        
        val channel = supabase.channel("chat-messages-$chatId")
        
        val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }
        
        // Subscribe to the channel first
        android.util.Log.d("ChatRepository", "Attempting to subscribe to channel...")
        channel.subscribe(blockUntilSubscribed = true)
        android.util.Log.d("ChatRepository", "âœ“ Channel subscribed successfully!")
        
        val job = launch {
            changeFlow.mapNotNull { action ->
                android.util.Log.d("ChatRepository", "Received Postgres action: ${action::class.simpleName}")
                when (action) {
                    is PostgresAction.Insert -> {
                        android.util.Log.d("ChatRepository", "INSERT action received")
                        action.record as? Message
                    }
                    is PostgresAction.Update -> {
                        android.util.Log.d("ChatRepository", "UPDATE action received")
                        action.record as? Message
                    }
                    else -> {
                        android.util.Log.d("ChatRepository", "Other action type: ${action::class.simpleName}")
                        null
                    }
                }
            }.collect { message ->
                android.util.Log.d("ChatRepository", "Message received - ID: ${message.id}, chat_id: ${message.chat_id}, sender: ${message.sender_id}, type: ${message.type}")
                // Filter for this specific chat on client side
                if (message.chat_id == chatId) {
                    android.util.Log.d("ChatRepository", "âœ“ Message matches chatId - sending to UI")
                    trySend(message)
                } else {
                    android.util.Log.d("ChatRepository", "âœ— Message chat_id (${message.chat_id}) doesn't match expected ($chatId)")
                }
            }
        }
        
        awaitClose {
            android.util.Log.d("ChatRepository", "=== REALTIME SUBSCRIPTION CLOSING ===")
            job.cancel()
            kotlinx.coroutines.runBlocking {
                channel.unsubscribe()
                android.util.Log.d("ChatRepository", "Channel unsubscribed")
            }
        }
    }

    // --- Follow Operations (Legacy/Social) ---
    
    suspend fun followUser(followerId: String, followingId: String): Result<Unit> = try {
        val follow = Follow(follower_id = followerId, following_id = followingId)
        supabase.from("follows").insert(follow)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unfollowUser(followerId: String, followingId: String): Result<Unit> = try {
        supabase.from("follows").delete {
            filter {
                eq("follower_id", followerId)
                eq("following_id", followingId)
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // --- Real-time Indicators ---

    suspend fun sendTyping(chatId: String, isTyping: Boolean) {
        try {
            val channel = supabase.channel("chat-$chatId")
            val payload = buildJsonObject {
                put("isTyping", isTyping)
            }
            channel.broadcast(event = "typing", message = payload)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun subscribeToTyping(chatId: String): Flow<Boolean> {
        val channel = supabase.channel("chat-$chatId")
        return channel.broadcastFlow<JsonObject>(event = "typing")
            .map { it["isTyping"]?.toString()?.toBoolean() ?: false }
    }
    
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean) {
        try {
            val update = mapOf(
                "is_online" to isOnline,
                "last_seen" to java.time.Instant.now().toString()
            )
            supabase.from("profiles").update(update) {
                filter { eq("id", userId) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun subscribeToUserStatus(userId: String): Flow<Profile> {
        val channel = supabase.channel("user-status-$userId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "profiles"
        }.map { action ->
            when (action) {
                is PostgresAction.Update -> action.record as Profile
                else -> throw IllegalStateException("Unexpected action type")
            }
        }.map { profile ->
            // Filter on client side for this specific user
            if (profile.id == userId) profile else throw IllegalStateException("Profile not for this user")
        }
    }

    private fun formatTimestamp(timestamp: String): String {
        return try {
            val instant = java.time.Instant.parse(timestamp)
            val zoneId = java.time.ZoneId.systemDefault()
            val dateTime = java.time.LocalDateTime.ofInstant(instant, zoneId)
            val now = java.time.LocalDateTime.now(zoneId)
            
            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            
            when {
                dateTime.toLocalDate().isEqual(now.toLocalDate()) -> {
                    dateTime.format(formatter)
                }
                dateTime.toLocalDate().isEqual(now.minusDays(1).toLocalDate()) -> {
                    "Yesterday"
                }
                else -> {
                    dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy"))
                }
            }
        } catch (e: Exception) {
            // Fallback to simple parsing if Instant.parse fails (e.g. if format is different)
            try {
                val parts = timestamp.split("T")
                if (parts.size > 1) {
                    val timePart = parts[1].split(".")[0]
                    timePart.substring(0, 5)
                } else {
                    "Now"
                }
            } catch (e2: Exception) {
                "Now"
            }
        }
    
    
    // --- WhatsApp Features ---
    
    // Edit message
    suspend fun editMessage(messageId: Long, newContent: String): Result<Unit> {
        return try {
            supabase.from("messages").update(
                mapOf(
                    "content" to newContent,
                    "edited_at" to java.time.Instant.now().toString()
                )
            ) {
                filter { eq("id", messageId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete message for me
    suspend fun deleteMessage(messageId: Long, userId: String): Result<Unit> {
        return try {
            val message = supabase.from("messages")
                .select() {
                    filter { eq("id", messageId) }
                }
                .decodeSingleOrNull<Message>()
            
            val deletedFor = message?.deleted_for?.toMutableList() ?: mutableListOf()
            deletedFor.add(userId)
            
            supabase.from("messages").update(
                mapOf("deleted_for" to deletedFor)
            ) {
                filter { eq("id", messageId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete message for everyone
    suspend fun deleteMessageForEveryone(messageId: Long): Result<Unit> {
        return try {
            supabase.from("messages").update(
                mapOf("is_deleted" to true)
            ) {
                filter { eq("id", messageId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Mark message as seen
    suspend fun markMessageAsSeen(messageId: Long): Result<Unit> {
        return try {
            supabase.from("messages").update(
                mapOf(
                    "seen_at" to java.time.Instant.now().toString(),
                    "status" to "seen"
                )
            ) {
                filter { eq("id", messageId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Mark all messages in chat as seen
    suspend fun markChatAsSeen(chatId: String, userId: String): Result<Unit> {
        return try {
            supabase.from("messages").update(
                mapOf(
                    "seen_at" to java.time.Instant.now().toString(),
                    "status" to "seen"
                )
            ) {
                filter {
                    eq("chat_id", chatId)
                    neq("sender_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Block user
    suspend fun blockUser(blockedUserId: String): Result<Unit> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            supabase.from("blocks").insert(
                mapOf(
                    "blocker_id" to currentUser!!.id,
                    "blocked_id" to blockedUserId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Unblock user
    suspend fun unblockUser(blockedUserId: String): Result<Unit> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }
            
            supabase.from("blocks").delete {
                filter {
                    eq("blocker_id", currentUser!!.id)
                    eq("blocked_id", blockedUserId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Check if user is blocked
    suspend fun isUserBlocked(userId: String): Result<Boolean> {
        return try {
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser == null) {
                return Result.success(false)
            }
            
            val block = supabase.from("blocks")
                .select() {
                    filter {
                        eq("blocker_id", currentUser!!.id)
                        eq("blocked_id", userId)
                    }
                }
                .decodeSingleOrNull<JsonObject>()
            
            Result.success(block != null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete entire chat
    suspend fun deleteChat(chatId: String): Result<Unit> {
        return try {
            supabase.from("chats").delete {
                filter { eq("id", chatId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get current user ID helper
    suspend fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }
}
}