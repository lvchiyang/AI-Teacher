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
    // 学生特有信息（仅当userType为STUDENT时有效）
    val studentId: String? = null,
    val studentName: String? = null,
    val grade: Int? = null,
    val currentChapter: String? = null
)

/**
 * 用户类型枚举
 */
enum class UserType {
    PARENT,     // 家长
    STUDENT     // 学生
}