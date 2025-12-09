package com.abcg.music.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abcg.music.data.repository.AuthRepository
import com.abcg.music.data.repository.ChatPreview
import com.abcg.music.data.repository.ChatRepository
import com.abcg.music.data.repository.Profile
import com.abcg.music.data.repository.SupabaseStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RangUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val chats: List<ChatPreview> = emptyList(),
    val searchResults: List<Profile> = emptyList(),
    val isSearching: Boolean = false,
    val currentProfile: Profile? = null,
    val viewedProfile: Profile? = null,
    val error: String? = null
)

class RangViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val storageRepository: SupabaseStorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RangUiState())
    val uiState: StateFlow<RangUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val isLoggedIn = authRepository.isUserLoggedIn()
        _uiState.value = _uiState.value.copy(isAuthenticated = isLoggedIn)
        
        if (isLoggedIn) {
            loadChats()
            loadCurrentProfile()
            updateOnlineStatus(true)
        }
    }
    
    private fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                chatRepository.updateOnlineStatus(currentUser.id, isOnline)
            }
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                chatRepository.getRecentChats(currentUser.id).fold(
                    onSuccess = { chats ->
                        _uiState.value = _uiState.value.copy(
                            chats = chats,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to load chats",
                            isLoading = false
                        )
                    }
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "User not authenticated",
                    isLoading = false,
                    isAuthenticated = false
                )
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)
            
            chatRepository.searchUsers(query).fold(
                onSuccess = { users ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = users,
                        isSearching = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Search failed",
                        isSearching = false
                    )
                }
            )
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.signIn(email, password).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        isLoading = false
                    )
                    loadChats()
                    loadCurrentProfile()
                    updateOnlineStatus(true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Sign in failed",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // First, sign up the user
                authRepository.signUp(email, password).getOrThrow()
                
                // Wait a moment for Supabase to create the user
                kotlinx.coroutines.delay(500)
                
                // Get the current user
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    // Update the profile with username
                    chatRepository.updateProfile(
                        Profile(
                            id = currentUser.id,
                            username = username,
                            full_name = username
                        )
                    ).getOrThrow()
                }
                
                // Now sign in
                signIn(email, password)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Sign up failed. Please try again.",
                    isLoading = false
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            updateOnlineStatus(false)
            authRepository.signOut()
            _uiState.value = RangUiState(isAuthenticated = false)
        }
    }

    fun sendMessage(receiverUsername: String, content: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(error = null)
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                chatRepository.sendMessageByUsername(currentUser.id, receiverUsername, content).fold(
                    onSuccess = {
                        loadChats() // Refresh chat list
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to send message. User may not exist."
                        )
                    }
                )
            }
        }
    }

    fun loadCurrentProfile() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                chatRepository.getProfile(currentUser.id).fold(
                    onSuccess = { profile ->
                        _uiState.value = _uiState.value.copy(currentProfile = profile)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to load profile"
                        )
                    }
                )
            }
        }
    }

    fun updateProfile(username: String, fullName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                val updatedProfile = Profile(
                    id = currentUser.id,
                    username = username,
                    full_name = fullName,
                    avatar_url = _uiState.value.currentProfile?.avatar_url,
                    bio = _uiState.value.currentProfile?.bio,
                    updated_at = _uiState.value.currentProfile?.updated_at
                )
                
                chatRepository.updateProfile(updatedProfile).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            currentProfile = updatedProfile,
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to update profile",
                            isLoading = false
                        )
                    }
                )
            }
        }
    }
    
    fun uploadProfileImage(byteArray: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                storageRepository.uploadProfileImage(currentUser.id, byteArray).fold(
                    onSuccess = { url ->
                        // Update profile with new avatar URL
                        val updatedProfile = _uiState.value.currentProfile?.copy(avatar_url = url) 
                            ?: Profile(id = currentUser.id, avatar_url = url)
                        
                        chatRepository.updateProfile(updatedProfile).fold(
                            onSuccess = {
                                _uiState.value = _uiState.value.copy(
                                    currentProfile = updatedProfile,
                                    isLoading = false
                                )
                            },
                            onFailure = { error ->
                                _uiState.value = _uiState.value.copy(
                                    error = error.message ?: "Failed to update profile with image",
                                    isLoading = false
                                )
                            }
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to upload image",
                            isLoading = false
                        )
                    }
                )
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            chatRepository.getProfile(userId).fold(
                onSuccess = { profile ->
                    _uiState.value = _uiState.value.copy(
                        viewedProfile = profile,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to load user profile",
                        isLoading = false
                    )
                }
            )
        }
    }

    fun blockUser(userId: String) {
        // TODO: Implement block user functionality
        viewModelScope.launch {
             // Placeholder for now
        }
    }
}
