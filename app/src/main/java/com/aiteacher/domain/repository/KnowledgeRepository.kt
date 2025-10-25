package com.aiteacher.domain.repository

import com.aiteacher.domain.model.Knowledge

/**
 * 知识点数据仓库接口
 */
interface KnowledgeRepository {
    suspend fun getAllKnowledge(): List<Knowledge>
    suspend fun getKnowledgeById(knowledgeId: String): Knowledge?
    suspend fun getKnowledgeBySubjectAndGrade(subject: String, grade: Int): List<Knowledge>
    suspend fun insertKnowledge(knowledge: Knowledge)
    suspend fun updateKnowledge(knowledge: Knowledge)
    suspend fun deleteKnowledge(knowledgeId: String)
}