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
    val taskId: String,
    
    @ColumnInfo(index = true)
    val studentId: String,
    
    val title: String,
    
    val description: String,
    
    val questionIds: List<String>, // 测试题目的ID列表
    
    val totalScore: Int?, // 总分
    
    val passingScore: Int?, // 通过分数
    
    val timeLimit: Int?, // 时间限制（分钟）
    
    val startedAt: Long?, // 开始时间
    
    val completedAt: Long?, // 完成时间
    
    val score: Int?, // 得分
    
    val completed: Boolean, // 是否完成
    
    val createdAt: Long,
    
    val updatedAt: Long
)