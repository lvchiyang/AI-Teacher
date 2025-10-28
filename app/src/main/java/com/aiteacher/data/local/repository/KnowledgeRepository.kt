package com.aiteacher.data.local.repository

import com.aiteacher.data.local.dao.KnowledgeDao
import com.aiteacher.data.local.entity.KnowledgeEntity
import com.aiteacher.domain.model.Knowledge

/**
 * 知识点仓库类
 * 处理知识点数据的业务逻辑
 */
class KnowledgeRepository(private val knowledgeDao: KnowledgeDao) {
    
    /**
     * 根据ID获取知识点
     */
    suspend fun getKnowledgeById(knowledgeId: String): Result<Knowledge?> {
        return try {
            val knowledge = knowledgeDao.getKnowledgeById(knowledgeId)
            Result.success(knowledge?.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据学科和年级获取知识点
     */
    suspend fun getKnowledgeBySubjectAndGrade(subject: String, grade: Int): Result<List<Knowledge>> {
        return try {
            val knowledgeList = knowledgeDao.getKnowledgeBySubjectAndGrade(subject, grade)
            Result.success(knowledgeList.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据章节获取知识点
     */
    suspend fun getKnowledgeByChapter(chapter: String): Result<List<Knowledge>> {
        return try {
            val knowledgeList = knowledgeDao.getKnowledgeByChapter(chapter)
            Result.success(knowledgeList.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取所有知识点
     */
    suspend fun getAllKnowledge(): Result<List<Knowledge>> {
        return try {
            val knowledgeList = knowledgeDao.getAllKnowledge()
            Result.success(knowledgeList.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 添加知识点
     */
    suspend fun insertKnowledge(knowledge: Knowledge): Result<Unit> {
        return try {
            val entity = knowledge.toEntity()
            knowledgeDao.insertKnowledge(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量添加知识点
     */
    suspend fun insertAllKnowledge(knowledgeList: List<Knowledge>): Result<Unit> {
        return try {
            val entities = knowledgeList.map { it.toEntity() }
            knowledgeDao.insertAllKnowledge(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新知识点
     */
    suspend fun updateKnowledge(knowledge: Knowledge): Result<Unit> {
        return try {
            val entity = knowledge.toEntity()
            knowledgeDao.updateKnowledge(entity)
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
    suspend fun searchKnowledge(keyword: String): Result<List<Knowledge>> {
        return try {
            val knowledgeList = knowledgeDao.searchKnowledge(keyword)
            Result.success(knowledgeList.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 根据知识点ID列表获取知识点
     */
    suspend fun getKnowledgeByIds(knowledgeIds: List<String>): Result<List<Knowledge>> {
        return try {
            val knowledgeList = knowledgeDao.getKnowledgeByIds(knowledgeIds)
            Result.success(knowledgeList.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 扩展函数：KnowledgeEntity 转 Knowledge
 */
private fun KnowledgeEntity.toDomainModel(): Knowledge {
    return Knowledge(
        knowledgeId = this.knowledgeId,
        subject = this.subject,
        grade = this.grade,
        chapter = this.chapter,
        concept = this.concept,
        applicationMethods = this.applicationMethods,
        keywords = this.keywords
    )
}

/**
 * 扩展函数：Knowledge 转 KnowledgeEntity
 */
private fun Knowledge.toEntity(): KnowledgeEntity {
    return KnowledgeEntity(
        knowledgeId = this.knowledgeId,
        subject = this.subject,
        grade = this.grade,
        chapter = this.chapter,
        concept = this.concept,
        applicationMethods = this.applicationMethods,
        keywords = this.keywords
    )
}