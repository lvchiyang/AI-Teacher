package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.entity.StudentEntity
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.model.LearningProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 学生数据仓库
 * 负责学生数据的本地存储和管理
 */
class StudentRepository(private val studentDao: StudentDao) {
    
    /**
     * 根据学生ID获取学生信息
     */
    suspend fun getStudentById(studentId: String): Result<Student?> {
        return try {
            val entity = studentDao.getStudentById(studentId)
            Result.success(entity?.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取学生信息（Flow版本）
     */
    fun getStudentByIdFlow(studentId: String): Flow<Result<Student?>> {
        return studentDao.getStudentByIdFlow(studentId).map { entity ->
            try {
                Result.success(entity?.toDomainModel())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取所有学生
     */
    suspend fun getAllStudents(): Result<List<Student>> {
        return try {
            val students = studentDao.getAllStudents().map { it.toDomainModel() }
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有学生（Flow版本）
     */
    fun getAllStudentsFlow(): Flow<Result<List<Student>>> {
        return studentDao.getAllStudentsFlow().map { entities ->
            try {
                val students = entities.map { it.toDomainModel() }
                Result.success(students)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 插入或更新学生信息
     */
    suspend fun saveStudent(student: Student): Result<Unit> {
        return try {
            val entity = student.toEntity()
            studentDao.insertStudent(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新学生信息
     */
    suspend fun updateStudent(student: Student): Result<Unit> {
        return try {
            val entity = student.toEntity()
            studentDao.updateStudent(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除学生
     */
    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            studentDao.deleteStudentById(studentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 检查学生是否存在
     */
    suspend fun studentExists(studentId: String): Result<Boolean> {
        return try {
            val exists = studentDao.getStudentById(studentId) != null
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}