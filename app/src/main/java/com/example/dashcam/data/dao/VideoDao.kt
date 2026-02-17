package com.example.dashcam.data.dao

import androidx.room.*
import com.example.dashcam.data.entity.Video
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: Video): Long

    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    suspend fun getVideoById(videoId: Long): Video?

    @Delete
    suspend fun delete(video: Video)

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteById(videoId: Long)

    @Query("DELETE FROM videos")
    suspend fun deleteAll()
}
