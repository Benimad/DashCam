package com.example.dashcam.ui.viewmodel

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcam.data.entity.Video
import com.example.dashcam.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CameraState(
    val isRecording: Boolean = false,
    val recordingDuration: Long = 0,
    val savedVideoPath: String? = null,
    val errorMessage: String? = null,
    val insufficientStorage: Boolean = false
)

class CameraViewModel(private val videoRepository: VideoRepository) : ViewModel() {
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private var recording: Recording? = null
    private var recordingStartTime: Long = 0

    fun getOutputFile(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "video_$timestamp.mp4"
        
        val moviesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "CarDashCam")
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "CarDashCam")
        }
        
        if (!moviesDir.exists()) {
            moviesDir.mkdirs()
        }
        
        return File(moviesDir, fileName)
    }
    
    fun hasEnoughStorage(context: Context, minRequiredMB: Long = 100): Boolean {
        val moviesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        }
        
        moviesDir?.let {
            val availableBytes = it.usableSpace
            val availableMB = availableBytes / (1024 * 1024)
            return availableMB >= minRequiredMB
        }
        
        return false
    }

    fun startRecording(context: Context? = null) {
        if (context != null && !hasEnoughStorage(context)) {
            _cameraState.value = _cameraState.value.copy(
                insufficientStorage = true,
                errorMessage = "Espace de stockage insuffisant (minimum 100 MB requis)"
            )
            return
        }
        
        _cameraState.value = _cameraState.value.copy(
            isRecording = true,
            recordingDuration = 0,
            errorMessage = null,
            insufficientStorage = false
        )
        recordingStartTime = System.currentTimeMillis()
    }

    fun stopRecording() {
        _cameraState.value = _cameraState.value.copy(isRecording = false)
        recording?.stop()
        recording = null
    }

    fun updateRecordingDuration() {
        if (_cameraState.value.isRecording) {
            val duration = System.currentTimeMillis() - recordingStartTime
            _cameraState.value = _cameraState.value.copy(recordingDuration = duration)
        }
    }

    fun setRecording(recording: Recording?) {
        this.recording = recording
    }

    fun saveVideoToDatabase(file: File, userId: Long) {
        viewModelScope.launch {
            try {
                val video = Video(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSize = file.length(),
                    timestamp = System.currentTimeMillis(),
                    duration = _cameraState.value.recordingDuration,
                    userId = userId
                )
                videoRepository.insert(video)
                _cameraState.value = _cameraState.value.copy(
                    savedVideoPath = file.absolutePath,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _cameraState.value = _cameraState.value.copy(
                    errorMessage = "Erreur lors de la sauvegarde: ${e.message}"
                )
            }
        }
    }

    fun clearSavedPath() {
        _cameraState.value = _cameraState.value.copy(savedVideoPath = null)
    }

    fun clearError() {
        _cameraState.value = _cameraState.value.copy(errorMessage = null)
    }
}
