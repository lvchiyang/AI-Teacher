package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.aiteacher.data.local.database.Converters
import com.aiteacher.domain.model.KnowledgeItem
import com.aiteacher.domain.model.LearningResource

/**
 * 教学任务数据库实体
 */
@Entity(
    tableName = "teaching_tasks",
    foreignKeys = [
        ForeignKey(
            entity = TeachingPlanEntity::class,
            parentColumns = ["planId"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class TeachingTaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "task_id")
    val taskId: String,
    
    @ColumnInfo(index = true)
    val planId: String,
    
    val day: Int,
    
    val date: String,
    
    val title: String,
    
    val description: String,
    
    val topics: List<String>,

    val relatedKnowledge: List<KnowledgeItem>,

    val estimatedTime: Int, // 预估学习时间（分钟）
    
    val content: String,
    
    val resources: List<LearningResource>,
    
    val completed: Boolean,

    val completionDate: String?,
    
    val grade: Int?,

    val maxGrade: Int?,

    val createdAt: Long,
    
    val updatedAt: Long
)