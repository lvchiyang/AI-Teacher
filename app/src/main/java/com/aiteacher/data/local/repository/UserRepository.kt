package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.UserDao
import com.aiteacher.data.local.entity.UserEntity
import com.aiteacher.data.local.entity.UserType
import com.aiteacher.domain.model.Student

/**
 * 用户仓库类
 * 处理用户数据的业务逻辑
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * 创建新用户
     */
    suspend fun createUser(
        userId: String,
        userType: UserType,
        studentId: String? = null
    ): Result<UserEntity> {
        return try {
            val user = UserEntity(
                userId = userId,
                userType = userType,
                studentId = studentId
            )
            
            userDao.insertUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取用户信息
     */
    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }

    /**
     * 更新用户信息
     */
    suspend fun updateUser(user: UserEntity): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除用户
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            userDao.deleteUserById(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取指定类型的用户
     */
    suspend fun getUsersByType(userType: UserType): List<UserEntity> {
        return userDao.getUsersByType(userType)
    }

    /**
     * 获取所有用户
     */
    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAllUsers()
    }
}