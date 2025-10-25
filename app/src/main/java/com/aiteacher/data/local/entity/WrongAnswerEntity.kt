package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo

/**
 * 错题记录数据库实体
 */
@Entity(
    tableName = "wrong_answers",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["studentId"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["questionId"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WrongAnswerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "student_id", index = true)
    val studentId: String,
    
    @ColumnInfo(name = "question_id", index = true)
    val questionId: String,
    
    @ColumnInfo(name = "student_answer")
    val studentAnswer: String,
    
    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean,
    
    @ColumnInfo(name = "time_spent")
    val timeSpent: Int,
    
    val attempt: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)