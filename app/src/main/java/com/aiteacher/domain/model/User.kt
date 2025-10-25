package com.aiteacher.domain.model

/**
 * 用户领域模型
 * 支持家长和学生两种用户类型
 */
data class User(
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