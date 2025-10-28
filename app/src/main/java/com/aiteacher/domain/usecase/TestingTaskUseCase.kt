package com.aiteacher.domain.usecase

import com.aiteacher.domain.model.TestingTask
import com.aiteacher.domain.model.Question
import com.aiteacher.data.local.repository.TestingTaskRepository
import com.aiteacher.data.local.repository.QuestionRepository

/**
 * 测试任务用例
 * 测试Agent的业务逻辑
 */
class TestingTaskUseCase(
    private val testingTaskRepository: TestingTaskRepository,
    private val questionRepository: QuestionRepository
) {
    
    /**
     * 创建测试任务 - 简化版本，只传入学生ID和知识点ID列表
     */
    suspend fun createTestingTask(
        studentId: String,
        knowledgePointIds: List<String>
    ): Result<TestingTask> {
        return try {
            // 获取相关题目
            val questions = mutableListOf<Question>()
            knowledgePointIds.forEach { knowledgeId ->
                val questionResult = questionRepository.getQuestionsByKnowledgeId(knowledgeId)
                if (questionResult.isSuccess) {
                    questions.addAll(questionResult.getOrNull() ?: emptyList())
                }
            }
            
            if (questions.isEmpty()) {
                return Result.failure(Exception("未找到相关测试题目"))
            }
            
            // 取前5题作为测试题
            val selectedQuestions = questions.take(5)
            val questionIds = selectedQuestions.map { it.questionId }
            
            // 计算总分（假设每题10分）
            val totalScore = selectedQuestions.size * 10
            
            val task = TestingTask(
                taskId = generateTaskId(),
                studentId = studentId,
                title = "知识点掌握测试",
                description = "检验对知识点的掌握情况",
                questionIds = questionIds,
                questions = selectedQuestions,
                totalScore = totalScore,
                passingScore = 60, // 60分及格
                timeLimit = null,
                startedAt = null,
                completedAt = null,
                score = null,
                completed = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // 保存到数据库
            testingTaskRepository.saveTestingTask(task)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建测试任务 - 完整版本
     */
    suspend fun createTestingTask(
        studentId: String,
        title: String,
        description: String,
        questionIds: List<String>,
        passingScore: Int = 60,
        timeLimit: Int? = null
    ): Result<TestingTask> {
        return try {
            // 获取题目详情
            val questions = questionIds.mapNotNull { questionId ->
                questionRepository.getQuestionById(questionId).getOrNull()
            }
            
            // 计算总分（假设每题10分）
            val totalScore = questions.size * 10
            
            val task = TestingTask(
                taskId = generateTaskId(),
                studentId = studentId,
                title = title,
                description = description,
                questionIds = questionIds,
                questions = questions,
                totalScore = totalScore,
                passingScore = passingScore,
                timeLimit = timeLimit,
                startedAt = null,
                completedAt = null,
                score = null,
                completed = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // 保存到数据库
            testingTaskRepository.saveTestingTask(task)
            Result.success(task)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 开始测试任务
     */
    suspend fun startTestingTask(taskId: String): Result<TestingTask?> {
        return try {
            val result = testingTaskRepository.startTestingTask(taskId)
            if (result.isSuccess && result.getOrNull() == true) {
                // 重新获取任务详情
                val taskResult = testingTaskRepository.getTestingTaskById(taskId)
                if (taskResult.isSuccess) {
                    taskResult
                } else {
                    Result.failure(Exception("无法获取测试任务信息"))
                }
            } else {
                Result.failure(Exception("无法开始测试任务"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 提交测试答案
     */
    suspend fun submitTestAnswers(
        taskId: String,
        answers: Map<String, String> // 题目ID -> 答案
    ): Result<TestingTaskResult> {
        return try {
            // 获取测试任务
            val taskResult = testingTaskRepository.getTestingTaskById(taskId)
            if (taskResult.isFailure) {
                return Result.failure(Exception("无法获取测试任务"))
            }
            
            val task = taskResult.getOrNull()
            if (task == null) {
                return Result.failure(Exception("测试任务不存在"))
            }
            
            if (task.completed) {
                return Result.failure(Exception("测试任务已经完成"))
            }
            
            // 计算得分
            var score = 0
            val questionResults = mutableMapOf<String, Boolean>()
            
            task.questions.forEach { question ->
                val userAnswer = answers[question.questionId]
                val isCorrect = userAnswer != null && checkAnswer(question, userAnswer)
                questionResults[question.questionId] = isCorrect
                
                if (isCorrect) {
                    // 每题10分
                    score += 10
                }
            }
            
            // 更新任务状态为已完成
            val completedAt = System.currentTimeMillis()
            val updateResult = testingTaskRepository.markTaskAsCompleted(taskId, completedAt, score)
            if (updateResult.isFailure) {
                return Result.failure(Exception("无法更新测试任务状态"))
            }
            
            // 判断是否通过
            val passed = score >= task.passingScore
            
            val result = TestingTaskResult(
                score = score,
                totalScore = task.totalScore,
                passed = passed,
                questionResults = questionResults,
                feedback = if (passed) "恭喜你通过了测试！" else "很遗憾，你没有通过测试，请继续努力！",
                completedAt = completedAt
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取未完成的测试任务
     */
    suspend fun getIncompleteTestingTasksByStudentId(studentId: String): Result<List<TestingTask>> {
        return testingTaskRepository.getIncompleteTestingTasksByStudentId(studentId)
    }
    
    /**
     * 根据学生ID获取已完成的测试任务
     */
    suspend fun getCompletedTestingTasksByStudentId(studentId: String): Result<List<TestingTask>> {
        return testingTaskRepository.getCompletedTestingTasksByStudentId(studentId)
    }
    
    /**
     * 获取测试任务详情
     */
    suspend fun getTestingTaskById(taskId: String): Result<TestingTask?> {
        return testingTaskRepository.getTestingTaskById(taskId)
    }
    
    /**
     * 检查答案是否正确
     * 简化版实现：直接比较字符串（实际应用中可能需要更复杂的逻辑）
     */
    private fun checkAnswer(question: Question, userAnswer: String): Boolean {
        return userAnswer.trim().equals(question.answer.trim(), ignoreCase = true)
    }
    
    /**
     * 生成任务ID
     */
    private fun generateTaskId(): String {
        return "test_${System.currentTimeMillis()}"
    }
}

/**
 * 测试任务结果模型
 */
data class TestingTaskResult(
    val score: Int, // 得分
    val totalScore: Int, // 总分
    val passed: Boolean, // 是否通过
    val questionResults: Map<String, Boolean>, // 各题目的答题结果
    val feedback: String, // 反馈信息
    val completedAt: Long // 完成时间
)