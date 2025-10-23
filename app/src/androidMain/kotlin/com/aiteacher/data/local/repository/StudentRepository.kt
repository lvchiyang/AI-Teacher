package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.entity.StudentData
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.model.LearningProgress
import org.jetbrains.exposed.sql.Database

/**
 * 学生数据仓库 - JVM版本
 * 负责学生数据的本地存储和管理
 */
class StudentRepository(private val database: Database) {
    
    private val studentDao = StudentDao(database)
    
    /**
     * 根据学生ID获取学生信息
     */
    suspend fun getStudentById(studentId: String): Student? {
        val entity = studentDao.getStudentById(studentId)
        return entity?.toDomainModel()
    }
    
    /**
     * 获取所有学生
     */
    suspend fun getAllStudents(): List<Student> {
        return studentDao.getAllStudents().map { it.toDomainModel() }
    }
    
    /**
     * 插入或更新学生信息
     */
    suspend fun saveStudent(student: Student) {
        val entity = student.toEntity()
        studentDao.insertStudent(entity)
    }
    
    /**
     * 更新学生信息
     */
    suspend fun updateStudent(student: Student) {
        val entity = student.toEntity()
        studentDao.updateStudent(entity)
    }
    
    /**
     * 删除学生
     */
    suspend fun deleteStudent(studentId: String) {
        studentDao.deleteStudentById(studentId)
    }
    
    /**
     * 检查学生是否存在
     */
    suspend fun studentExists(studentId: String): Boolean {
        return studentDao.getStudentById(studentId) != null
    }
}

/**
 * 扩展函数：StudentData 转 Student
 */
private fun StudentData.toDomainModel(): Student {
    return Student(
        studentId = this.studentId,
        studentName = this.studentName,
        grade = this.grade,
        currentChapter = this.currentChapter,
        learningProgress = LearningProgress(
            notTaught = emptyList(),
            taughtToReview = emptyList(),
            notMastered = emptyList(),
            basicMastery = emptyList(),
            fullMastery = emptyList(),
            lastUpdateTime = this.updatedAt.toString()
        )
    )
}

/**
 * 扩展函数：Student 转 StudentData
 */
private fun Student.toEntity(): StudentData {
    return StudentData(
        studentId = this.studentId,
        studentName = this.studentName,
        grade = this.grade,
        currentChapter = this.currentChapter,
        createdAt = java.time.LocalDateTime.now(),
        updatedAt = java.time.LocalDateTime.now()
    )
}
