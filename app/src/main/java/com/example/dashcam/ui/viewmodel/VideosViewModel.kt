package com.example.dashcam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcam.data.entity.Video
import com.example.dashcam.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VideosState(
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class VideosViewModel(private val videoRepository: VideoRepository) : ViewModel() {
    private val _videosState = MutableStateFlow(VideosState())
    val videosState: StateFlow<VideosState> = _videosState.asStateFlow()

    init {
        loadVideos()
    }

    private fun loadVideos() {
        viewModelScope.launch {
            _videosState.value = _videosState.value.copy(isLoading = true)
            try {
                videoRepository.getAllVideos().collect { videos ->
                    _videosState.value = _videosState.value.copy(
                        videos = videos,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _videosState.value = _videosState.value.copy(
                    isLoading = false,
                    errorMessage = "Erreur lors du chargement des vidéos: ${e.message}"
                )
            }
        }
    }

    fun deleteVideo(video: Video) {
        viewModelScope.launch {
            try {
                videoRepository.delete(video)
                val file = java.io.File(video.filePath)
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                _videosState.value = _videosState.value.copy(
                    errorMessage = "Erreur lors de la suppression: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _videosState.value = _videosState.value.copy(errorMessage = null)
    }
}
