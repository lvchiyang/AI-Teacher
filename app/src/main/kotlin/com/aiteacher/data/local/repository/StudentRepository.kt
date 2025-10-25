package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.entity.StudentEntity
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.model.LearningProgress

/**
 * 学生数据仓库 - Room版本
 * 负责学生数据的本地存储和管理
 */
class StudentRepository(private val studentDao: StudentDao) {
    
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
        return studentDao.studentExists(studentId)
    }
}

/**
 * 扩展函数：StudentEntity 转 Student
 */
private fun StudentEntity.toDomainModel(): Student {
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
 * 扩展函数：Student 转 StudentEntity
 */
private fun Student.toEntity(): StudentEntity {
    return StudentEntity(
        studentId = this.studentId,
        studentName = this.studentName,
        grade = this.grade,
        currentChapter = this.currentChapter,
        createdAt = java.util.Date(),
        updatedAt = java.util.Date()
    )
}
