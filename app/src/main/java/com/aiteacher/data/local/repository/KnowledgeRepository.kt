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
    suspend fun getKnowledgeById(knowledgeId: String): Result<KnowledgeEntity?> {
        return try {
            val knowledge = knowledgeDao.getKnowledgeById(knowledgeId)
            Result.success(knowledge)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学科和年级获取知识点
     */
    suspend fun getKnowledgeBySubjectAndGrade(subject: String, grade: Int): Result<List<KnowledgeEntity>> {
        return try {
            val knowledgeList = knowledgeDao.getKnowledgeBySubjectAndGrade(subject, grade)
            Result.success(knowledgeList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据章节获取知识点
     */
    suspend fun getKnowledgeByChapter(chapter: String): Result<List<KnowledgeEntity>> {
        return try {
            val knowledgeList = knowledgeDao.getKnowledgeByChapter(chapter)
            Result.success(knowledgeList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有知识点
     */
    suspend fun getAllKnowledge(): Result<List<KnowledgeEntity>> {
        return try {
            val knowledgeList = knowledgeDao.getAllKnowledge()
            Result.success(knowledgeList)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    
    /**
     * 根据关键词搜索知识点
     */
    suspend fun searchKnowledge(keyword: String): Result<List<KnowledgeEntity>> {
        return try {
            val knowledgeList = knowledgeDao.searchKnowledge(keyword)
            Result.success(knowledgeList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据知识点ID列表获取知识点
     */
    suspend fun getKnowledgeByIds(knowledgeIds: List<String>): Result<List<KnowledgeEntity>> {
        return try {
            val knowledgeList = knowledgeDao.getKnowledgeByIds(knowledgeIds)
            Result.success(knowledgeList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}