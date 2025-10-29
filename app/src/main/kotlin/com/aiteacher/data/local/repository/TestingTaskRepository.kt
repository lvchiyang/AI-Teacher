package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.TestingTaskDao
import com.aiteacher.data.local.entity.TestingTaskEntity
import com.aiteacher.domain.model.TestingTask
import com.aiteacher.domain.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 测试任务数据仓库
 * 处理测试任务数据的业务逻辑
 */
class TestingTaskRepository(
    private val testingTaskDao: TestingTaskDao,
    private val questionRepository: QuestionRepository
) {
    
    /**
     * 根据ID获取测试任务
     */
    suspend fun getTestingTaskById(taskId: String): Result<TestingTask?> {
        return try {
            val task = testingTaskDao.getTestingTaskById(taskId)
            if (task != null) {
                val questions = getQuestionsForTask(task.questionIds)
                Result.success(task.toDomainModel(questions))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据ID获取测试任务（Flow版本）
     */
    fun getTestingTaskByIdFlow(taskId: String): Flow<Result<TestingTask?>> {
        return testingTaskDao.getTestingTaskByIdFlow(taskId).map { task ->
            try {
                if (task != null) {
                    val questions = getQuestionsForTask(task.questionIds)
                    Result.success(task.toDomainModel(questions))
                } else {
                    Result.success(null)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据学生ID获取测试任务列表
     */
    suspend fun getTestingTasksByStudentId(studentId: String): Result<List<TestingTask>> {
        return try {
            val tasks = testingTaskDao.getTestingTasksByStudentId(studentId)
            val tasksWithQuestions = tasks.map { task ->
                val questions = getQuestionsForTask(task.questionIds)
                task.toDomainModel(questions)
            }
            Result.success(tasksWithQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID获取测试任务列表（Flow版本）
     */
    fun getTestingTasksByStudentIdFlow(studentId: String): Flow<Result<List<TestingTask>>> {
        return testingTaskDao.getTestingTasksByStudentIdFlow(studentId).map { tasks ->
            try {
                val tasksWithQuestions = tasks.map { task ->
                    val questions = getQuestionsForTask(task.questionIds)
                    task.toDomainModel(questions)
                }
                Result.success(tasksWithQuestions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取学生未完成的测试任务
     */
    suspend fun getIncompleteTestingTasksByStudentId(studentId: String): Result<List<TestingTask>> {
        return try {
            val tasks = testingTaskDao.getIncompleteTestingTasksByStudentId(studentId)
            val tasksWithQuestions = tasks.map { task ->
                val questions = getQuestionsForTask(task.questionIds)
                task.toDomainModel(questions)
            }
            Result.success(tasksWithQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取学生已完成的测试任务
     */
    suspend fun getCompletedTestingTasksByStudentId(studentId: String): Result<List<TestingTask>> {
        return try {
            val tasks = testingTaskDao.getCompletedTestingTasksByStudentId(studentId)
            val tasksWithQuestions = tasks.map { task ->
                val questions = getQuestionsForTask(task.questionIds)
                task.toDomainModel(questions)
            }
            Result.success(tasksWithQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据完成状态获取测试任务
     */
    suspend fun getTestingTasksByCompletionStatus(completed: Boolean): Result<List<TestingTask>> {
        return try {
            val tasks = testingTaskDao.getTestingTasksByCompletionStatus(completed)
            val tasksWithQuestions = tasks.map { task ->
                val questions = getQuestionsForTask(task.questionIds)
                task.toDomainModel(questions)
            }
            Result.success(tasksWithQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有测试任务
     */
    suspend fun getAllTestingTasks(): Result<List<TestingTask>> {
        return try {
            val tasks = testingTaskDao.getAllTestingTasks()
            val tasksWithQuestions = tasks.map { task ->
                val questions = getQuestionsForTask(task.questionIds)
                task.toDomainModel(questions)
            }
            Result.success(tasksWithQuestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有测试任务（Flow版本）
     */
    fun getAllTestingTasksFlow(): Flow<Result<List<TestingTask>>> {
        return testingTaskDao.getAllTestingTasksFlow().map { tasks ->
            try {
                val tasksWithQuestions = tasks.map { task ->
                    val questions = getQuestionsForTask(task.questionIds)
                    task.toDomainModel(questions)
                }
                Result.success(tasksWithQuestions)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 保存测试任务
     */
    suspend fun saveTestingTask(task: TestingTask): Result<Unit> {
        return try {
            val entity = task.toEntity()
            testingTaskDao.insertTestingTask(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量保存测试任务
     */
    suspend fun saveTestingTasks(tasks: List<TestingTask>): Result<Unit> {
        return try {
            val entities = tasks.map { it.toEntity() }
            testingTaskDao.insertTestingTasks(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新测试任务
     */
    suspend fun updateTestingTask(task: TestingTask): Result<Unit> {
        return try {
            val entity = task.toEntity()
            testingTaskDao.updateTestingTask(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除测试任务
     */
    suspend fun deleteTestingTask(taskId: String): Result<Unit> {
        return try {
            testingTaskDao.deleteTestingTaskById(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学生ID删除测试任务
     */
    suspend fun deleteTestingTasksByStudentId(studentId: String): Result<Unit> {
        return try {
            testingTaskDao.deleteTestingTasksByStudentId(studentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 标记测试任务为已完成
     */
    suspend fun markTaskAsCompleted(taskId: String, completedAt: Long?, score: Int?): Result<Boolean> {
        return try {
            val task = testingTaskDao.getTestingTaskById(taskId)
            if (task != null) {
                val updatedTask = task.copy(
                    completed = true,
                    completedAt = completedAt,
                    score = score,
                    updatedAt = System.currentTimeMillis()
                )
                testingTaskDao.updateTestingTask(updatedTask)
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 开始测试任务
     */
    suspend fun startTestingTask(taskId: String): Result<Boolean> {
        return try {
            val task = testingTaskDao.getTestingTaskById(taskId)
            if (task != null) {
                val updatedTask = task.copy(
                    startedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                testingTaskDao.updateTestingTask(updatedTask)
                Result.success(true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据问题ID列表获取问题
     */
    private suspend fun getQuestionsForTask(questionIds: List<String>): List<Question> {
        return questionIds.mapNotNull { questionId ->
            questionRepository.getQuestionById(questionId).getOrNull()
        }
    }
}

/**
 * 扩展函数：TestingTaskEntity 转 TestingTask
 */
private fun TestingTaskEntity.toDomainModel(questions: List<Question>): TestingTask {
    return TestingTask(
        taskId = this.taskId,
        studentId = this.studentId,
        title = this.title,
        description = this.description,
        questionIds = this.questionIds,
        questions = questions,
        totalScore = this.totalScore,
        passingScore = this.passingScore,
        timeLimit = this.timeLimit,
        startedAt = this.startedAt,
        completedAt = this.completedAt,
        score = this.score,
        completed = this.completed,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * 扩展函数：TestingTask 转 TestingTaskEntity
 */
private fun TestingTask.toEntity(): TestingTaskEntity {
    return TestingTaskEntity(
        taskId = this.taskId,
        studentId = this.studentId,
        title = this.title,
        description = this.description,
        questionIds = this.questionIds,
        totalScore = this.totalScore,
        passingScore = this.passingScore,
        timeLimit = this.timeLimit,
        startedAt = this.startedAt,
        completedAt = this.completedAt,
        score = this.score,
        completed = this.completed,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}