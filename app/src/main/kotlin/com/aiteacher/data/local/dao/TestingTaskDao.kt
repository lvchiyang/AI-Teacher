package com.aiteacher.data.local.dao

import androidx.room.*
import com.aiteacher.data.local.entity.TestingTaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * 测试任务数据访问对象
 */
@Dao
interface TestingTaskDao {
    
    @Query("SELECT * FROM testing_tasks WHERE taskId = :taskId")
    suspend fun getTestingTaskById(taskId: String): TestingTaskEntity?
    
    @Query("SELECT * FROM testing_tasks WHERE taskId = :taskId")
    fun getTestingTaskByIdFlow(taskId: String): Flow<TestingTaskEntity?>
    
    @Query("SELECT * FROM testing_tasks WHERE studentId = :studentId ORDER BY createdAt DESC")
    suspend fun getTestingTasksByStudentId(studentId: String): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getTestingTasksByStudentIdFlow(studentId: String): Flow<List<TestingTaskEntity>>
    
    @Query("SELECT * FROM testing_tasks WHERE studentId = :studentId AND completed = 0 ORDER BY createdAt DESC")
    suspend fun getIncompleteTestingTasksByStudentId(studentId: String): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks WHERE studentId = :studentId AND completed = 1 ORDER BY createdAt DESC")
    suspend fun getCompletedTestingTasksByStudentId(studentId: String): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks WHERE completed = :completed ORDER BY createdAt DESC")
    suspend fun getTestingTasksByCompletionStatus(completed: Boolean): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks ORDER BY createdAt DESC")
    suspend fun getAllTestingTasks(): List<TestingTaskEntity>
    
    @Query("SELECT * FROM testing_tasks ORDER BY createdAt DESC")
    fun getAllTestingTasksFlow(): Flow<List<TestingTaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestingTask(task: TestingTaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestingTasks(tasks: List<TestingTaskEntity>)
    
    @Update
    suspend fun updateTestingTask(task: TestingTaskEntity)
    
    @Delete
    suspend fun deleteTestingTask(task: TestingTaskEntity)
    
    @Query("DELETE FROM testing_tasks WHERE taskId = :taskId")
    suspend fun deleteTestingTaskById(taskId: String)
    
    @Query("DELETE FROM testing_tasks WHERE studentId = :studentId")
    suspend fun deleteTestingTasksByStudentId(studentId: String)
}