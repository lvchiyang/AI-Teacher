package com.aiteacher.domain.usecase

import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.data.local.repository.TeachingTaskRepository
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.model.TeachingTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 学生用例
 * 处理学生相关的业务逻辑
 */
class StudentUseCase(
    private val studentRepository: StudentRepository,
    private val teachingTaskRepository: TeachingTaskRepository) {
    
    /**
     * 创建新学生
     */
    suspend fun createStudent(studentId: String, studentName: String, grade: Int): Result<Student> {
        return try {
            // 检查学生是否已存在，使用Result.isFailure方法判断
            val studentResult = studentRepository.getStudentById(studentId)
            if (studentResult.isSuccess && studentResult.getOrNull() != null) {
                return Result.failure(Exception("学生已存在"))
            }
            
            val student = Student(
                studentId = studentId,
                studentName = studentName,
                grade = grade,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            val saveResult = studentRepository.saveStudent(student)
            if (saveResult.isSuccess) {
                Result.success(student)
            } else {
                Result.failure(Exception("保存学生信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取学生信息
     */
    suspend fun getStudent(studentId: String): Result<Student?> {
        return try {
            val studentResult = studentRepository.getStudentById(studentId)
            if (studentResult.isSuccess) {
                val student = studentResult.getOrNull()
                Result.success(student)
            } else {
                Result.failure(Exception("获取学生信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取学生信息（Flow版本）
     */
    fun getStudentFlow(studentId: String): Flow<Student?> {
        return studentRepository.getStudentByIdFlow(studentId).map{ result ->
            result.getOrNull()
        }
    }
    
    /**
     * 更新学生学习进度
     * 当完成一个知识点时调用
     */
    suspend fun updateTeachingTask(task: TeachingTask): Result<Unit> {
        return try {
            val updateResult = teachingTaskRepository.updateTeachingTask(task)
            if (updateResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("更新教学任务失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 完成教学任务后更新进度
     */
    suspend fun completeTeachingTask(task: TeachingTask): Result<Unit> {
        val updatedTask = task.copy(completed = true, completionDate = System.currentTimeMillis().toString())
        return try {
            val updateResult = teachingTaskRepository.updateTeachingTask(updatedTask)
            if (updateResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("完成教学任务失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 完成测试任务
     */
    suspend fun completeTestingTask(
        studentId: String,
        passed: Boolean
    ): Result<Unit> {
        return try {
            // 这里应该更新学生的学习进度
            // 暂时返回成功结果
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
            val updateResult = studentRepository.updateStudent(student)
            if (updateResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("更新学生信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除学生
     */
    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            val deleteResult = studentRepository.deleteStudent(studentId)
            if (deleteResult.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除学生失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有学生
     */
    suspend fun getAllStudents(): Result<List<Student>?> {
        return try {
            val studentsResult = studentRepository.getAllStudents()
            if (studentsResult.isSuccess) {
                val students = studentsResult.getOrNull()
                Result.success(students)
            } else {
                Result.failure(Exception("获取所有学生信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有学生（Flow版本）
     */
    fun getAllStudentsFlow(): Flow<List<Student>?> {
        return studentRepository.getAllStudentsFlow().map { result ->
            result.getOrNull()
        }
    }
}