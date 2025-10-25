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
            // MVP阶段：返回包含题目的测试任务
            val questions = listOf(
                TestQuestion(
                    questionId = "test_q_1",
                    knowledgePointId = "7_1_1_1",
                    content = "请计算：2 + 3 = ?",
                    image = null,
                    type = QuestionType.CALCULATION,
                    correctAnswer = "5",
                    explanation = "2 + 3 = 5",
                    points = 10,
                    timeLimit = 5
                ),
                TestQuestion(
                    questionId = "test_q_2",
                    knowledgePointId = "7_1_1_2",
                    content = "请计算：4 × 2 = ?",
                    image = null,
                    type = QuestionType.CALCULATION,
                    correctAnswer = "8",
                    explanation = "4 × 2 = 8",
                    points = 10,
                    timeLimit = 5
                ),
                TestQuestion(
                    questionId = "test_q_3",
                    knowledgePointId = "7_1_1_3",
                    content = "请计算：10 - 3 = ?",
                    image = null,
                    type = QuestionType.CALCULATION,
                    correctAnswer = "7",
                    explanation = "10 - 3 = 7",
                    points = 10,
                    timeLimit = 5
                )
            )
            
            val task = TestingTask(
                taskId = taskId,
                studentId = "student_1",
                knowledgePointIds = listOf("7_1_1_1", "7_1_1_2", "7_1_1_3"),
                questions = questions,
                status = TaskStatus.IN_PROGRESS,
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
            // MVP阶段：简化答案判断逻辑
            val isCorrect = answer.isNotEmpty() && answer.trim().length >= 1
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
                resultId = generateResultId(),
                taskId = taskId,
                studentId = studentId,
                totalScore = score,
                maxScore = 100,
                correctCount = if (isCorrect) 1 else 0,
                totalCount = 1,
                timeSpent = 30,
                answers = listOf(studentAnswer),
                timestamp = getCurrentTime()
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
    
    private fun generateResultId(): String {
        return "result_${System.currentTimeMillis()}"
    }
}
