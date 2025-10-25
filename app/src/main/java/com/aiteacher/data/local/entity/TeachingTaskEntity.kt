package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.aiteacher.data.local.database.Converters

/**
 * 教学任务数据库实体
 */
@Entity(
    tableName = "teaching_tasks",
    foreignKeys = [
        ForeignKey(
            entity = TeachingPlanEntity::class,
            parentColumns = ["plan_id"],
            childColumns = ["plan_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class TeachingTaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: String,
    
    @ColumnInfo(name = "plan_id", index = true)
    val planId: String,
    
    val day: Int,
    
    val date: String,
    
    val title: String,
    
    val description: String,
    
    val topics: List<String>,
    
    @ColumnInfo(name = "related_knowledge")
    val relatedKnowledge: List<KnowledgeItem>,
    
    @ColumnInfo(name = "estimated_time")
    val estimatedTime: Int, // 预估学习时间（分钟）
    
    val content: String,
    
    val resources: List<LearningResource>,
    
    val completed: Boolean,
    
    @ColumnInfo(name = "completion_date")
    val completionDate: String?,
    
    val grade: Int,
    
    @ColumnInfo(name = "max_grade")
    val maxGrade: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

/**
 * 知识点关联项数据类
 */
data class KnowledgeItem(
    @ColumnInfo(name = "knowledge_id")
    val knowledgeId: String,
    val topic: String,
    val subject: String
)

/**
 * 学习资源数据类
 */
data class LearningResource(
    val type: String,
    val url: String,
    val title: String
)