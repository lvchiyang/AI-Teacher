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
    @ColumnInfo(name = "student_id")
    val studentId: String,
    
    @ColumnInfo(name = "student_name")
    val studentName: String,
    
    @ColumnInfo(name = "grade")
    val grade: Int,
    
    @ColumnInfo(name = "current_chapter")
    val currentChapter: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
)
