package com.aiteacher.domain.usecase

import com.aiteacher.ai.agent.TestingAgent
import com.aiteacher.data.local.repository.TestingTaskRepository

/**
 * 批改测试任务用例
 * 实现传入学生的答题结果，送入TestingAgent获取批改结果，将分数等相关信息更新到数据库
 */
class GradeTestingTaskUseCase(
    private val testingAgent: TestingAgent,
    private val testingTaskRepository: TestingTaskRepository
){
    /**
     * 批改测试任务的参数数据类
     */
    data class Params(
        val taskId: String,                     // 测试任务ID
        val studentAnswers: Map<String, String> // 学生答案，key为题目ID，value为学生答案
    )
    
    /**
     * 执行批改测试任务的操作
     * @param params 批改测试任务的参数
     * @return Result包装的Unit，表示操作是否成功
     */
    suspend operator fun invoke(params: Params): Result<Unit> {
        return try {
            // 1. 获取测试任务
            val taskResult = testingTaskRepository.getTestingTaskById(params.taskId)
            if (taskResult.isFailure) {
                return Result.failure(taskResult.exceptionOrNull() ?: Exception("Failed to get testing task"))
            }
            
            val task = taskResult.getOrNull()
            if (task == null) {
                return Result.failure(Exception("Testing task not found"))
            }
            
            // 2. 更新题目中的学生答案
            val questionsWithAnswers = task.questions.map { question ->
                val studentAnswer = params.studentAnswers[question.questionId]
                question.copy(studentAnswer = studentAnswer)
            }
            
            // 3. 调用TestingAgent批改答案
            val gradingResults = testingAgent.gradeStudentAnswers(questionsWithAnswers)
            
            if (gradingResults.isFailure) {
                return Result.failure(gradingResults.exceptionOrNull() ?: Exception("Failed to grade student answers"))
            }
            
            val results = gradingResults.getOrNull() ?: emptyList()
            
            // 4. 计算总分
            val totalScore = results.sumOf { it.score }
            
            // 5. 更新测试任务
            val updatedTask = task.copy(
                questions = questionsWithAnswers,
                score = totalScore,
                completed = true,
                completedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            // 6. 保存到数据库
            testingTaskRepository.updateTestingTask(updatedTask)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}