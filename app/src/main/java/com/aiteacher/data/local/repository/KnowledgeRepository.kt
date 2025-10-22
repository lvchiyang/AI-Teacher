package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.KnowledgeDao
import com.aiteacher.data.local.entity.KnowledgeEntity

/**
 * 知识点仓库类
 * 处理知识点数据的业务逻辑
 */
class KnowledgeRepository(private val knowledgeDao: KnowledgeDao) {
    
    /**
     * 根据ID获取知识点
     */
    suspend fun getKnowledgeById(knowledgeId: String): KnowledgeEntity? {
        return knowledgeDao.getKnowledgeById(knowledgeId)
    }
    
    /**
     * 根据学科和年级获取知识点
     */
    suspend fun getKnowledgeBySubjectAndGrade(subject: String, grade: Int): List<KnowledgeEntity> {
        return knowledgeDao.getKnowledgeBySubjectAndGrade(subject, grade)
    }
    
    /**
     * 根据章节获取知识点
     */
    suspend fun getKnowledgeByChapter(chapter: String): List<KnowledgeEntity> {
        return knowledgeDao.getKnowledgeByChapter(chapter)
    }
    
    /**
     * 获取所有知识点
     */
    suspend fun getAllKnowledge(): List<KnowledgeEntity> {
        return knowledgeDao.getAllKnowledge()
    }
    
    /**
     * 添加知识点
     */
    suspend fun insertKnowledge(knowledge: KnowledgeEntity): Result<Unit> {
        return try {
            knowledgeDao.insertKnowledge(knowledge)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量添加知识点
     */
    suspend fun insertAllKnowledge(knowledgeList: List<KnowledgeEntity>): Result<Unit> {
        return try {
            knowledgeDao.insertAllKnowledge(knowledgeList)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新知识点
     */
    suspend fun updateKnowledge(knowledge: KnowledgeEntity): Result<Unit> {
        return try {
            knowledgeDao.updateKnowledge(knowledge)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除知识点
     */
    suspend fun deleteKnowledge(knowledgeId: String): Result<Unit> {
        return try {
            knowledgeDao.deleteKnowledgeById(knowledgeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}