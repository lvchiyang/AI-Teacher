package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户数据库实体
 * 支持家长和学生两种用户类型
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val userType: UserType,
    // 当用户类型为学生时，关联的学生ID
    val studentId: String? = null
)

/**
 * 用户类型枚举
 */
enum class UserType {
    PARENT,     // 家长
    STUDENT     // 学生
}