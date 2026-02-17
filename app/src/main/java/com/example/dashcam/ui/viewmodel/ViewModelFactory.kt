package com.example.dashcam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dashcam.data.repository.UserRepository
import com.example.dashcam.data.repository.VideoRepository
import com.example.dashcam.util.SessionManager

class ViewModelFactory(
    private val userRepository: UserRepository,
    private val videoRepository: VideoRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(userRepository, sessionManager) as T
            }
            modelClass.isAssignableFrom(CameraViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                CameraViewModel(videoRepository) as T
            }
            modelClass.isAssignableFrom(VideosViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                VideosViewModel(videoRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
