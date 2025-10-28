package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.aiteacher.data.local.database.Converters

/**
 * 测试任务数据库实体
 */
@Entity(tableName = "testing_tasks")
@TypeConverters(Converters::class)
data class TestingTaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: String,
    
    @ColumnInfo(name = "student_id", index = true)
    val studentId: String,
    
    val title: String,
    
    val description: String,
    
    @ColumnInfo(name = "question_ids")
    val questionIds: List<String>, // 测试题目的ID列表
    
    @ColumnInfo(name = "total_score")
    val totalScore: Int, // 总分
    
    @ColumnInfo(name = "passing_score")
    val passingScore: Int, // 通过分数
    
    @ColumnInfo(name = "time_limit")
    val timeLimit: Int?, // 时间限制（分钟）
    
    @ColumnInfo(name = "started_at")
    val startedAt: Long?, // 开始时间
    
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?, // 完成时间
    
    val score: Int?, // 得分
    
    val completed: Boolean, // 是否完成
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)