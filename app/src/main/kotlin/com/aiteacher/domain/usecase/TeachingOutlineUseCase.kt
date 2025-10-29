package com.aiteacher.domain.usecase

import com.aiteacher.ai.agent.SecretaryAgent
import com.aiteacher.ai.agent.TeachingPlanResult
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.domain.model.LearningProgress

/**
 * 教学大纲用例
 * 协调Repository和Agent生成教学大纲
 */
class TeachingOutlineUseCase(
    private val studentRepository: StudentRepository,
    private val secretaryAgent: SecretaryAgent
) {
    
    /**
     * 生成教学大纲
     */
    suspend fun generateOutline(
        studentId: String,
        grade: Int
    ): Result<TeachingPlanResult> {
        return try {
            android.util.Log.d("TeachingOutlineUseCase", "=== 开始生成教学大纲 ===")
            android.util.Log.d("TeachingOutlineUseCase", "学生ID: $studentId, 年级: $grade")
            
            // 1. 获取学生信息
            val student = studentRepository.getStudentById(studentId)
            if (student == null) {
                android.util.Log.e("TeachingOutlineUseCase", "学生信息未找到: $studentId")
                return Result.failure(IllegalArgumentException("Student not found"))
            }
            android.util.Log.d("TeachingOutlineUseCase", "成功获取学生信息: ${student.studentName}")
            
            // 2. 获取学习进度
            val learningProgress = student.learningProgress ?: LearningProgress(
                notTaught = emptyList(),
                taughtToReview = emptyList(),
                notMastered = emptyList(),
                basicMastery = emptyList(),
                fullMastery = emptyList(),
                lastUpdateTime = ""
            )
            
            // 3. 调用Agent生成教学计划
            android.util.Log.d("TeachingOutlineUseCase", "开始调用SecretaryAgent生成教学计划")
            val result = secretaryAgent.createTeachingPlan(
                studentId = studentId,
                grade = grade,
                currentChapter = student.currentChapter ?: "第一章 有理数",
                learningProgress = learningProgress
            )
            
            if (result.isSuccess) {
                val planResult = result.getOrThrow()
                android.util.Log.d("TeachingOutlineUseCase", "教学计划生成成功: ${planResult.planDescription}")
                Result.success(planResult)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("TeachingOutlineUseCase", "教学计划生成失败: $error")
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to generate teaching plan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

