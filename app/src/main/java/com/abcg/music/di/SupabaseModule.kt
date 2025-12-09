package com.abcg.music.di

import com.abcg.music.data.repository.AuthRepository
import com.abcg.music.data.repository.ChatRepository
import com.abcg.music.viewModel.ChatViewModel
import com.abcg.music.viewModel.RangViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.realtime.Realtime
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://ubrpxismcjfsxjjdkuxc.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVicnB4aXNtY2pmc3hqamRrdXhjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM0NTkyMTUsImV4cCI6MjA3OTAzNTIxNX0.AJd9m1l3MjVFK_t-L3lYnHrdbQ0DptFP0KvXJEbDEyA"
        ) {
            install(Postgrest)
            install(Storage)
            install(Auth)
            install(Realtime) {
                // Enable auto-reconnection for real-time
            }
        }
    }
    
    single { AuthRepository(get()) }
    single { ChatRepository(get()) }
    single { com.abcg.music.data.repository.SupabaseStorageRepository(get()) }
    viewModel { RangViewModel(get(), get(), get()) }
    viewModel { (otherUserId: String) -> ChatViewModel(otherUserId, get(), get(), get()) }
}
