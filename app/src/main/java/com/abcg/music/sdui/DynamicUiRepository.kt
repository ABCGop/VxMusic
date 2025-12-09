package com.abcg.music.sdui

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class DynamicUiRepository {

    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val json = Json { ignoreUnknownKeys = true }

    fun getDynamicScreen(screenId: String): Flow<DynamicScreen?> = callbackFlow {
        val ref = database.getReference("config/dynamic_ui/$screenId")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val jsonString = snapshot.value as? String
                    // Firebase sometimes returns a Map if we don't save it as a JSON string. 
                    // For robust SDUI, storing the config as a JSON string is often safer for complex nested structures,
                    // but we can also try to parse from the Map if needed.
                    // For this implementation, we'll assume the user pastes a JSON string into the value,
                    // OR maps the object structure directly.
                    
                    // Simple approach: Deserialize from the Map structure using standard Firebase conversion if possible,
                    // but since we heavily use kotlinx.serialization for polymorphism, we might prefer fetching as a raw Map and converting.
                    
                    // Let's try to simple approach: Manual construct or use a JSON string in DB.
                    // To keep it user-friendly, let's assume the user puts a JSON string in 'payload' field,
                    // or we try to parse the whole node.
                    
                    // BETTER APPROACH: Read 'payload' string field to utilize full JSON power
                    // structure: config/dynamic_ui/home_screen/payload = "{...}"
                    
                    val payload = snapshot.child("payload").getValue(String::class.java)
                    if (payload != null) {
                        val screen = json.decodeFromString<DynamicScreen>(payload)
                        trySend(screen)
                    } else {
                        // Fallback: try to see if it's a direct object (less likely to match strict JSON types)
                         trySend(null)
                    }
                } catch (e: Exception) {
                    Log.e("DynamicUiRepository", "Error parsing dynamic UI", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DynamicUiRepository", "Firebase error", error.toException())
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
