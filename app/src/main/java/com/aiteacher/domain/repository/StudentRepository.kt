package com.aiteacher.domain.repository

import com.aiteacher.domain.model.Student

/**
 * 学生数据仓库接口
 */
interface StudentRepository {
    suspend fun getAllStudents(): List<Student>
    suspend fun getStudentById(studentId: String): Student?
    suspend fun getStudentByName(studentName: String): List<Student>
    suspend fun insertStudent(student: Student)
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(studentId: String)
}