package com.aiteacher.data.local.entity

import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

/**
 * 学生数据库表定义 - 使用Exposed ORM
 */
object StudentEntity : Table("students") {
    val studentId = varchar("student_id", 50).uniqueIndex()
    val studentName = varchar("student_name", 100)
    val grade = integer("grade")
    val currentChapter = varchar("current_chapter", 100)
    val createdAt = varchar("created_at", 50).default(LocalDateTime.now().toString())
    val updatedAt = varchar("updated_at", 50).default(LocalDateTime.now().toString())
    
    override val primaryKey = PrimaryKey(studentId)
}

/**
 * 学生数据类
 */
data class StudentData(
    val studentId: String,
    val studentName: String,
    val grade: Int,
    val currentChapter: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
