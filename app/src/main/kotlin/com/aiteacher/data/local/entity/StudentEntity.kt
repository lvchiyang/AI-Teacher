package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

/**
 * 学生数据库实体 - 使用Room
 */
@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val studentId: String,
    val studentName: String,
    val grade: Int,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
