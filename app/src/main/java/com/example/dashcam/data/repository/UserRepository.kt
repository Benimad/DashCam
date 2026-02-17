package com.example.dashcam.data.repository

import com.example.dashcam.data.dao.UserDao
import com.example.dashcam.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    suspend fun insert(user: User): Long = withContext(Dispatchers.IO) {
        return@withContext userDao.insert(user)
    }

    suspend fun getUserByEmail(email: String): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserByEmail(email)
    }

    fun getUserById(userId: Long): Flow<User?> {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByIdDirect(userId: Long): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserByIdDirect(userId)
    }

    suspend fun getAnyUser(): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.getAnyUser()
    }
}
