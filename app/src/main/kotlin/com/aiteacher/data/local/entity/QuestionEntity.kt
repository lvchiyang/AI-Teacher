package com.aiteacher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.TypeConverters
import com.aiteacher.data.local.database.Converters

/**
 * 题目实体类
 * 对应题库表(QuestionBase)
 */
@Entity(tableName = "question_base")
@TypeConverters(Converters::class)
data class QuestionEntity(
    @PrimaryKey
    val questionId: String,             // 题目ID，如 "Q1001"
    val subject: String,               // 学科，如 "数学"
    val grade: Int,                    // 年级，如 7
    val questionText: String,          // 题目内容
    val answer: String,                // 答案
    @ColumnInfo(name = "question_type")
    val questionType: String,          // 题目类型，单选题/多选题/判断题/填空题/简答题
    val difficulty: Int?,               // 难度等级
    @ColumnInfo(name = "related_knowledge_ids")
    val relatedKnowledgeIds: List<String> // 关联的知识点ID列表
)