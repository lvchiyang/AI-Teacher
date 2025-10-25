package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo

/**
 * 教学计划数据库实体
 */
@Entity(
    tableName = "teaching_plans",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TeachingPlanEntity(
    @PrimaryKey
    @ColumnInfo(name = "plan_id")
    val planId: String,
    
    @ColumnInfo(name = "student_id", index = true)
    val studentId: String,
    
    val title: String,
    
    val description: String,
    
    val subject: String,
    
    @ColumnInfo(name = "grade_level")
    val gradeLevel: String,
    
    @ColumnInfo(name = "total_days")
    val totalDays: Int,
    
    @ColumnInfo(name = "start_date")
    val startDate: String,
    
    @ColumnInfo(name = "end_date")
    val endDate: String,
    
    val status: String, // active, completed, paused
    
    @ColumnInfo(name = "total_tasks")
    val totalTasks: Int = 0, // 计划下的任务总个数
    
    @ColumnInfo(name = "completed_tasks")
    val completedTasks: Int = 0, // 已完成任务数
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    val tags: List<String> = emptyList()
)