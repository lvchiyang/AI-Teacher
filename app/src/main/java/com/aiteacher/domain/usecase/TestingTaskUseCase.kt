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
        knowledgePointIds: List<String>
    ): Result<TestingTask> {
        return try {
            val questions = knowledgePointIds.map { knowledgePointId ->
                TestQuestion(
                    questionId = "test_q_${knowledgePointId}",
                    knowledgePointId = knowledgePointId,
                    content = "请解答关于 $knowledgePointId 的题目",
                    image = null,
                    type = QuestionType.CALCULATION,
                    correctAnswer = "正确答案",
                    explanation = "这是题目解析",
                    points = 10,
                    timeLimit = 5
                )
            }
            
            val task = TestingTask(
                taskId = generateTaskId(),
                studentId = studentId,
                knowledgePointIds = knowledgePointIds,
                questions = questions,
                status = TaskStatus.PENDING,
                currentQuestionIndex = 0,
                startTime = getCurrentTime(),
                timeLimit = questions.sumOf { it.timeLimit }
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
            // 这里应该从数据库获取任务并更新状态
            // 暂时返回模拟数据
            val task = TestingTask(
                taskId = taskId,
                studentId = "student_1",
                knowledgePointIds = listOf("7_1_1_1", "7_1_1_2"),
                questions = emptyList(),
                status = TaskStatus.IN_PROGRESS,
                currentQuestionIndex = 0,
                startTime = getCurrentTime(),
                timeLimit = 10
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
            // 模拟AI评判答案
            val isCorrect = answer.isNotEmpty() && answer.length > 2
            val score = if (isCorrect) 10 else 0
            
            val studentAnswer = StudentAnswer(
                answerId = generateAnswerId(),
                questionId = questionId,
                studentId = studentId,
                answer = answer,
                imageAnswer = imageAnswer,
                isCorrect = isCorrect,
                score = score,
                timeSpent = 30, // 模拟用时
                timestamp = getCurrentTime()
            )
            
            val result = TestingResult(
                studentAnswer = studentAnswer,
                isCorrect = isCorrect,
                score = score,
                feedback = if (isCorrect) "回答正确！" else "答案有误，请查看解析",
                explanation = "这是详细的题目解析",
                shouldUpdateProgress = true
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
     * 生成答案ID
     */
    private fun generateAnswerId(): String {
        return "answer_${System.currentTimeMillis()}"
    }
    
    /**
     * 获取当前时间
     */
    private fun getCurrentTime(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}

/**
 * 检验结果模型
 */
data class TestingResult(
    val studentAnswer: StudentAnswer,
    val isCorrect: Boolean,
    val score: Int,
    val feedback: String,
    val explanation: String,
    val shouldUpdateProgress: Boolean
)
