package com.aiteacher.domain.usecase

import com.aiteacher.domain.model.*

/**
 * 检验任务用例
 * 检验Agent的业务逻辑
 */
class TestingTaskUseCase {
    
    /**
     * 创建检验任务
     */
    suspend fun createTestingTask(
        studentId: String,
        title: String,
        description: String,
        questionIds: List<String>
    ): Result<TestingTask> {
        return try {
            val questions = questionIds.map { qid ->
                Question(
                    questionId = qid,
                    subject = "",
                    grade = 0,
                    questionText = "",
                    answer = "",
                    questionType = "",
                    difficulty = null,
                    relatedKnowledgeIds = emptyList()
                )
            }
            
            val task = TestingTask(
                taskId = generateTaskId(),
                studentId = studentId,
                title = title,
                description = description,
                questionIds = questionIds,
                questions = questions,
                totalScore = 0,
                passingScore = 0,
                timeLimit = null,
                startedAt = null,
                completedAt = null,
                score = null,
                completed = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 开始检验任务
     */
    suspend fun startTestingTask(taskId: String): Result<TestingTask> {
        return try {
            val questions = listOf(
                Question(
                    questionId = "test_q_1",
                    subject = "",
                    grade = 0,
                    questionText = "请计算：2 + 3 = ?",
                    answer = "5",
                    questionType = "calc",
                    difficulty = null,
                    relatedKnowledgeIds = emptyList()
                ),
                Question(
                    questionId = "test_q_2",
                    subject = "",
                    grade = 0,
                    questionText = "请计算：4 × 2 = ?",
                    answer = "8",
                    questionType = "calc",
                    difficulty = null,
                    relatedKnowledgeIds = emptyList()
                ),
                Question(
                    questionId = "test_q_3",
                    subject = "",
                    grade = 0,
                    questionText = "请计算：10 - 3 = ?",
                    answer = "7",
                    questionType = "calc",
                    difficulty = null,
                    relatedKnowledgeIds = emptyList()
                )
            )
            
            val task = TestingTask(
                taskId = taskId,
                studentId = "student_1",
                title = "测验",
                description = "基础计算",
                questionIds = questions.map { it.questionId },
                questions = questions,
                totalScore = 0,
                passingScore = 0,
                timeLimit = null,
                startedAt = System.currentTimeMillis(),
                completedAt = null,
                score = null,
                completed = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 提交学生答案
     */
    suspend fun submitStudentAnswer(
        taskId: String,
        questionId: String,
        studentId: String,
        answer: String,
        imageAnswer: String? = null
    ): Result<TestingResult> {
        return try {
            // 简化：构造最小化结果模型（若你的领域有对应模型，按需调整）
            val isCorrect = answer.isNotEmpty()
            val result = TestingResult(
                isCorrect = isCorrect,
                feedback = if (isCorrect) "回答正确" else "请再试一次"
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 完成检验任务
     */
    suspend fun completeTestingTask(
        taskId: String,
        studentId: String,
        results: List<TestingResult>
    ): Result<Unit> {
        return try {
            // MVP简化：暂时不更新数据库，直接返回成功
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 生成任务ID
     */
    private fun generateTaskId(): String {
        return "test_${System.currentTimeMillis()}"
    }
    
    /**
     * 获取当前时间
     */
    private fun getCurrentTime(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}

data class TestingResult(
    val isCorrect: Boolean,
    val feedback: String
)
