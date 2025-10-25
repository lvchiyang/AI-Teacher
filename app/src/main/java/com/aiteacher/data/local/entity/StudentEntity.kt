package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 学生数据库实体
 */
@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val studentId: String,
    val studentName: String,
    val grade: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)