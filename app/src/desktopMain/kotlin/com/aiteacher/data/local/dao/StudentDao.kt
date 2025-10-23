package com.aiteacher.data.local.dao

import com.aiteacher.data.local.entity.StudentData
import com.aiteacher.data.local.entity.StudentEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * 学生数据访问对象 - 使用Exposed ORM
 */
class StudentDao(private val database: org.jetbrains.exposed.sql.Database) {
    
    init {
        // 创建表
        transaction(database) {
            SchemaUtils.create(StudentEntity)
        }
    }
    
    suspend fun getStudentById(studentId: String): StudentData? {
        return transaction(database) {
            StudentEntity.selectAll().where { StudentEntity.studentId eq studentId }
                .map { row ->
                    StudentData(
                        studentId = row[StudentEntity.studentId],
                        studentName = row[StudentEntity.studentName],
                        grade = row[StudentEntity.grade],
                        currentChapter = row[StudentEntity.currentChapter],
                        createdAt = LocalDateTime.parse(row[StudentEntity.createdAt]),
                        updatedAt = LocalDateTime.parse(row[StudentEntity.updatedAt])
                    )
                }.firstOrNull()
        }
    }
    
    suspend fun getAllStudents(): List<StudentData> {
        return transaction(database) {
            StudentEntity.selectAll()
                .map { row ->
                    StudentData(
                        studentId = row[StudentEntity.studentId],
                        studentName = row[StudentEntity.studentName],
                        grade = row[StudentEntity.grade],
                        currentChapter = row[StudentEntity.currentChapter],
                        createdAt = LocalDateTime.parse(row[StudentEntity.createdAt]),
                        updatedAt = LocalDateTime.parse(row[StudentEntity.updatedAt])
                    )
                }
        }
    }
    
    suspend fun insertStudent(student: StudentData) {
        transaction(database) {
            StudentEntity.insert {
                it[studentId] = student.studentId
                it[studentName] = student.studentName
                it[grade] = student.grade
                it[currentChapter] = student.currentChapter
                it[createdAt] = student.createdAt.toString()
                it[updatedAt] = student.updatedAt.toString()
            }
        }
    }
    
    suspend fun updateStudent(student: StudentData) {
        transaction(database) {
            StudentEntity.update({ StudentEntity.studentId eq student.studentId }) {
                it[studentName] = student.studentName
                it[grade] = student.grade
                it[currentChapter] = student.currentChapter
                it[updatedAt] = LocalDateTime.now().toString()
            }
        }
    }
    
    suspend fun deleteStudentById(studentId: String) {
        transaction(database) {
            StudentEntity.deleteWhere { StudentEntity.studentId eq studentId }
        }
    }
}
