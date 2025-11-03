package com.aiteacher.domain.usecase

import com.aiteacher.ai.agent.TestingAgent
import com.aiteacher.data.local.repository.TestingTaskRepository
import com.aiteacher.data.local.repository.QuestionRepository
import com.aiteacher.domain.model.Knowledge
import com.aiteacher.domain.model.TestingTask
import java.util.UUID

/**
 * 创建测试任务用例（通过Agent生成题目）
 */
class CreateTestingTaskWithAgentUseCase(
    private val testingAgent: TestingAgent,
    private val testingTaskRepository: TestingTaskRepository,
    private val questionRepository: QuestionRepository
) {
    
    /**
     * 创建测试任务的参数数据类
     */
    data class Params(
        val studentId: String,
        val title: String,
        val description: String,
        val knowledge: Knowledge, // 知识点信息，用于生成题目
        val totalScore: Int? = 100,
        val passingScore: Int? = 60,
        val timeLimit: Int? = 60
    )
    
    /**
     * 执行创建测试任务的操作
     * @param params 创建测试任务的参数
     * @return Result包装的Unit，表示操作是否成功
     */
    suspend operator fun invoke(params: Params): Result<Unit> {
        return try {
            // 1. 调用TestingAgent生成测试题目
            val questionsResult = testingAgent.generateTestQuestions(params.knowledge)
            
            if (questionsResult.isFailure) {
                return Result.failure(questionsResult.exceptionOrNull() ?: Exception("Failed to generate questions"))
            }
            
            // 2. 为每个题目分配唯一ID并保存到数据库
            val originalQuestions = questionsResult.getOrNull() ?: emptyList()
            val updatedQuestions = originalQuestions.map { question ->
                // 为每个题目生成唯一ID
                val newQuestionId = "Q-" + UUID.randomUUID().toString()
                question.copy(
                    questionId = newQuestionId,
                    subject = params.knowledge.subject,
                    grade = params.knowledge.grade
                )
            }
            
            // 3. 将题目保存到数据库
            questionRepository.insertAllQuestions(updatedQuestions)
            
            val questionIds = updatedQuestions.map { it.questionId }
            
            // 4. 自动生成唯一的 taskId
            val taskId = "TT-" + UUID.randomUUID().toString()
            
            // 5. 创建测试任务对象
            val newTestingTask = TestingTask(
                taskId = taskId,
                studentId = params.studentId,
                title = params.title,
                description = params.description,
                questionIds = questionIds,
                questions = updatedQuestions,
                totalScore = params.totalScore,
                passingScore = params.passingScore,
                timeLimit = params.timeLimit
            )
            
            // 6. 保存到数据库
            testingTaskRepository.saveTestingTask(newTestingTask)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}