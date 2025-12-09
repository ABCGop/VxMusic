package com.abcg.music.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.minutes

class SupabaseStorageRepository(
    private val supabase: SupabaseClient
) {
    private val chatMediaBucket = "chat-media"
    private val profileImagesBucket = "profile-images"

    suspend fun uploadProfileImage(userId: String, byteArray: ByteArray): Result<String> {
        return try {
            val path = "${userId}/${System.currentTimeMillis()}.jpg"
            val bucket = supabase.storage.from("avatars")
            bucket.upload(path, byteArray) {
                upsert = true
            }
            Result.success(bucket.publicUrl(path))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadChatMedia(chatId: String, byteArray: ByteArray, extension: String = "jpg"): Result<String> {
        return try {
            val fileName = "$chatId/${System.currentTimeMillis()}.$extension"
            val bucket = supabase.storage.from(chatMediaBucket)
            bucket.upload(fileName, byteArray) {
                upsert = true
            }
            val url = bucket.publicUrl(fileName)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadVoiceNote(chatId: String, byteArray: ByteArray): Result<String> {
        return try {
            val fileName = "$chatId/voice/${System.currentTimeMillis()}.m4a"
            val bucket = supabase.storage.from(chatMediaBucket)
            bucket.upload(fileName, byteArray) {
                upsert = true
            }
            val url = bucket.publicUrl(fileName)
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
