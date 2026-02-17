package com.example.dashcam.data.dao

import androidx.room.*
import com.example.dashcam.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<User?>
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdDirect(userId: Long): User?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getAnyUser(): User?
}
