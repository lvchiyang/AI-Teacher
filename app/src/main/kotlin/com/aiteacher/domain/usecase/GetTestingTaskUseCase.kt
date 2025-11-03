package com.aiteacher.domain.usecase

import com.aiteacher.data.local.repository.TestingTaskRepository
import com.aiteacher.domain.model.TestingTask
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 获取测试任务用例
 * 实现根据TaskId查询TestingTask，以及根据StudentId查询最近的且未完成的TestingTask
 */
class GetTestingTaskUseCase : KoinComponent {
    
    private val testingTaskRepository: TestingTaskRepository by inject()
    
    /**
     * 根据TaskId获取测试任务
     * @param taskId 测试任务ID
     * @return Result包装的TestingTask对象，可能为null
     */
    suspend operator fun invoke(taskId: String): Result<TestingTask?> {
        return try {
            testingTaskRepository.getTestingTaskById(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取最近的未完成测试任务
     * @param studentId 学生ID
     * @return Result包装的TestingTask对象，可能为null（如果没有未完成的任务）
     */
    suspend fun getLatestIncompleteTaskByStudentId(studentId: String): Result<TestingTask?> {
        return try {
            val result = testingTaskRepository.getIncompleteTestingTasksByStudentId(studentId)
            if (result.isSuccess) {
                val tasks = result.getOrNull() ?: emptyList()
                // 返回最近的一个任务（列表已经是按时间倒序排列的）
                Result.success(tasks.firstOrNull())
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to get incomplete testing tasks"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取所有未完成的测试任务
     * @param studentId 学生ID
     * @return Result包装的TestingTask列表
     */
    suspend fun getAllIncompleteTasksByStudentId(studentId: String): Result<List<TestingTask>> {
        return try {
            testingTaskRepository.getIncompleteTestingTasksByStudentId(studentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}