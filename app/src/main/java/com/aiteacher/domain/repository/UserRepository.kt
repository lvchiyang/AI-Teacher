package com.aiteacher.domain.repository

import com.aiteacher.domain.model.User

/**
 * 用户数据仓库接口
 */
interface UserRepository {
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(userId: String): User?
    suspend fun getUserByStudentId(studentId: String): User?
    suspend fun insertUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(userId: String)
}