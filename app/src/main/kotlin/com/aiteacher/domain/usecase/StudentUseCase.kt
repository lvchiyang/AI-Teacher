package com.aiteacher.domain.usecase

import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.domain.model.Student
import com.aiteacher.domain.model.MasteryStatus

/**
 * 学生用例
 * 处理学生相关的业务逻辑
 */
class StudentUseCase(private val studentRepository: StudentRepository) {
    
    /**
     * 创建新学生
     */
    suspend fun createStudent(studentId: String, studentName: String, grade: Int): Result<Student> {
        return try {
            // 检查学生是否已存在
            if (studentRepository.studentExists(studentId)) {
                return Result.failure(Exception("学生已存在"))
            }
            
            val student = Student(
                studentId = studentId,
                studentName = studentName,
                grade = grade,
                currentChapter = "第一章 有理数", // 默认章节
                learningProgress = com.aiteacher.domain.model.LearningProgress(
                    notTaught = listOf("7_1_1_1", "7_1_1_2", "7_1_1_3"),
                    taughtToReview = emptyList(),
                    notMastered = emptyList(),
                    basicMastery = emptyList(),
                    fullMastery = emptyList(),
                    lastUpdateTime = System.currentTimeMillis().toString()
                )
            )
            
            studentRepository.saveStudent(student)
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取学生信息
     */
    suspend fun getStudent(studentId: String): Result<Student?> {
        return try {
            val student = studentRepository.getStudentById(studentId)
            Result.success(student)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    
    /**
     * 更新学生学习进度
     * 当完成一个知识点时调用
     */
    suspend fun updateStudentProgress(
        studentId: String, 
        knowledgePointId: String, 
        newStatus: MasteryStatus
    ): Result<Unit> {
        return try {
            val student = studentRepository.getStudentById(studentId)
                ?: return Result.failure(Exception("学生不存在"))
            
            val currentProgress = student.learningProgress
                ?: return Result.failure(Exception("学习进度不存在"))
            
            val updatedProgress = when (newStatus) {
                MasteryStatus.TAUGHT_TO_REVIEW -> {
                    // 从未讲解移动到已讲解待复习
                    currentProgress.copy(
                        notTaught = currentProgress.notTaught - knowledgePointId,
                        taughtToReview = currentProgress.taughtToReview + knowledgePointId,
                        lastUpdateTime = System.currentTimeMillis().toString()
                    )
                }
                MasteryStatus.BASIC_MASTERY -> {
                    // 从已讲解待复习移动到初步掌握
                    currentProgress.copy(
                        taughtToReview = currentProgress.taughtToReview - knowledgePointId,
                        basicMastery = currentProgress.basicMastery + knowledgePointId,
                        lastUpdateTime = System.currentTimeMillis().toString()
                    )
                }
                MasteryStatus.FULL_MASTERY -> {
                    // 从初步掌握移动到熟练掌握
                    currentProgress.copy(
                        basicMastery = currentProgress.basicMastery - knowledgePointId,
                        fullMastery = currentProgress.fullMastery + knowledgePointId,
                        lastUpdateTime = System.currentTimeMillis().toString()
                    )
                }
                MasteryStatus.NOT_MASTERED -> {
                    // 从任何状态移动到未掌握
                    currentProgress.copy(
                        notTaught = currentProgress.notTaught - knowledgePointId,
                        taughtToReview = currentProgress.taughtToReview - knowledgePointId,
                        basicMastery = currentProgress.basicMastery - knowledgePointId,
                        notMastered = currentProgress.notMastered + knowledgePointId,
                        lastUpdateTime = System.currentTimeMillis().toString()
                    )
                }
                else -> currentProgress
            }
            
            val updatedStudent = student.copy(learningProgress = updatedProgress)
            studentRepository.updateStudent(updatedStudent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 完成教学任务后更新进度
     */
    suspend fun completeTeachingTask(studentId: String, knowledgePointId: String): Result<Unit> {
        return updateStudentProgress(studentId, knowledgePointId, MasteryStatus.TAUGHT_TO_REVIEW)
    }
    
    /**
     * 完成测试任务后更新进度
     */
    suspend fun completeTestingTask(studentId: String, knowledgePointId: String, isCorrect: Boolean): Result<Unit> {
        val newStatus = if (isCorrect) MasteryStatus.BASIC_MASTERY else MasteryStatus.NOT_MASTERED
        return updateStudentProgress(studentId, knowledgePointId, newStatus)
    }
    
    /**
     * 更新学生信息
     */
    suspend fun updateStudent(student: Student): Result<Unit> {
        return try {
            studentRepository.updateStudent(student)
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
            studentRepository.deleteStudent(studentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有学生
     */
    suspend fun getAllStudents(): Result<List<Student>> {
        return try {
            val students = studentRepository.getAllStudents()
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}
