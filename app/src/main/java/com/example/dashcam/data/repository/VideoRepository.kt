package com.example.dashcam.data.repository

import com.example.dashcam.data.dao.VideoDao
import com.example.dashcam.data.entity.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VideoRepository(private val videoDao: VideoDao) {
    suspend fun insert(video: Video): Long = withContext(Dispatchers.IO) {
        return@withContext videoDao.insert(video)
    }

    fun getAllVideos(): Flow<List<Video>> {
        return videoDao.getAllVideos()
    }

    suspend fun getVideoById(videoId: Long): Video? = withContext(Dispatchers.IO) {
        return@withContext videoDao.getVideoById(videoId)
    }

    suspend fun delete(video: Video) = withContext(Dispatchers.IO) {
        videoDao.delete(video)
    }

    suspend fun deleteById(videoId: Long) = withContext(Dispatchers.IO) {
        videoDao.deleteById(videoId)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        videoDao.deleteAll()
    }
}
