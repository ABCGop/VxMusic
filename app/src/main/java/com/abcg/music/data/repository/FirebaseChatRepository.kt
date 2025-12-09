package com.abcg.music.data.repository

import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log
import java.time.Instant

data class FirebaseMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val type: String = "text",
    val mediaUrl: String = "",
    val status: String = "sent",
    val timestamp: Long = System.currentTimeMillis()
)

class FirebaseChatRepository {
    private val database = FirebaseDatabase.getInstance()
    private val messagesRef = database.getReference("messages")
    private val chatsRef = database.getReference("chats")
    
    // Send message to Firebase
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        content: String?,
        type: String = "text",
        mediaUrl: String? = null
    ): Result<String> = try {
        val messageId = messagesRef.child(chatId).push().key ?: throw Exception("Failed to generate message ID")
        
        val message = FirebaseMessage(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            content = content ?: "",
            type = type,
            mediaUrl = mediaUrl ?: "",
            status = "sent",
            timestamp = System.currentTimeMillis()
        )
        
        messagesRef.child(chatId).child(messageId).setValue(message).await()
        
        // Update last message in chat
        val chatUpdate = mapOf(
            "lastMessage" to (content ?: ""),
            "lastMessageTime" to message.timestamp,
            "lastMessageType" to type
        )
        chatsRef.child(chatId).updateChildren(chatUpdate).await()
        
        Log.d("FirebaseChat", "Message sent successfully: $messageId")
        Result.success(messageId)
    } catch (e: Exception) {
        Log.e("FirebaseChat", "Failed to send message", e)
        Result.failure(e)
    }
    
    // Get messages for a chat
    suspend fun getMessages(chatId: String): Result<List<Message>> = try {
        val snapshot = messagesRef.child(chatId)
            .orderByChild("timestamp")
            .get()
            .await()
        
        val messages = mutableListOf<Message>()
        snapshot.children.forEach { child ->
            val firebaseMsg = child.getValue(FirebaseMessage::class.java)
            if (firebaseMsg != null) {
                messages.add(
                    Message(
                        id = firebaseMsg.id.hashCode().toLong(),
                        sender_id = firebaseMsg.senderId,
                        receiver_id = firebaseMsg.receiverId,
                        chat_id = firebaseMsg.chatId,
                        content = firebaseMsg.content,
                        type = firebaseMsg.type,
                        media_url = firebaseMsg.mediaUrl.takeIf { it.isNotEmpty() },
                        status = firebaseMsg.status,
                        created_at = Instant.ofEpochMilli(firebaseMsg.timestamp).toString()
                    )
                )
            }
        }
        
        Log.d("FirebaseChat", "Loaded ${messages.size} messages for chat $chatId")
        Result.success(messages)
    } catch (e: Exception) {
        Log.e("FirebaseChat", "Failed to load messages", e)
        Result.failure(e)
    }
    
    // Real-time message listener
    fun subscribeToMessages(chatId: String): Flow<Message> = callbackFlow {
        Log.d("FirebaseChat", "=== FIREBASE REALTIME SUBSCRIPTION START ===")
        Log.d("FirebaseChat", "Subscribing to chatId: $chatId")
        
        val messageListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val firebaseMsg = snapshot.getValue(FirebaseMessage::class.java)
                if (firebaseMsg != null) {
                    Log.d("FirebaseChat", "New message received: ${firebaseMsg.id}")
                    val message = Message(
                        id = firebaseMsg.id.hashCode().toLong(),
                        sender_id = firebaseMsg.senderId,
                        receiver_id = firebaseMsg.receiverId,
                        chat_id = firebaseMsg.chatId,
                        content = firebaseMsg.content,
                        type = firebaseMsg.type,
                        media_url = firebaseMsg.mediaUrl.takeIf { it.isNotEmpty() },
                        status = firebaseMsg.status,
                        created_at = Instant.ofEpochMilli(firebaseMsg.timestamp).toString()
                    )
                    trySend(message)
                }
            }
            
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val firebaseMsg = snapshot.getValue(FirebaseMessage::class.java)
                if (firebaseMsg != null) {
                    Log.d("FirebaseChat", "Message updated: ${firebaseMsg.id}")
                    val message = Message(
                        id = firebaseMsg.id.hashCode().toLong(),
                        sender_id = firebaseMsg.senderId,
                        receiver_id = firebaseMsg.receiverId,
                        chat_id = firebaseMsg.chatId,
                        content = firebaseMsg.content,
                        type = firebaseMsg.type,
                        media_url = firebaseMsg.mediaUrl.takeIf { it.isNotEmpty() },
                        status = firebaseMsg.status,
                        created_at = Instant.ofEpochMilli(firebaseMsg.timestamp).toString()
                    )
                    trySend(message)
                }
            }
            
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseChat", "Firebase listener cancelled: ${error.message}")
            }
        }
        
        messagesRef.child(chatId).addChildEventListener(messageListener)
        Log.d("FirebaseChat", "✓ Firebase listener attached successfully!")
        
        awaitClose {
            Log.d("FirebaseChat", "=== FIREBASE SUBSCRIPTION CLOSING ===")
            messagesRef.child(chatId).removeEventListener(messageListener)
            Log.d("FirebaseChat", "Firebase listener removed")
        }
    }
}
