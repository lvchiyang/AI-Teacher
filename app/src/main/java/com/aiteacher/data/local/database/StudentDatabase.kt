package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.aiteacher.data.local.dao.KnowledgeDao
import com.aiteacher.data.local.dao.QuestionDao
import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.dao.TeachingPlanDao
import com.aiteacher.data.local.dao.TeachingTaskDao
import com.aiteacher.data.local.dao.WrongAnswerDao
import com.aiteacher.data.local.dao.TestingTaskDao
import com.aiteacher.data.local.entity.KnowledgeEntity
import com.aiteacher.data.local.entity.QuestionEntity
import com.aiteacher.data.local.entity.StudentEntity
import com.aiteacher.data.local.entity.TeachingPlanEntity
import com.aiteacher.data.local.entity.TeachingTaskEntity
import com.aiteacher.data.local.entity.WrongAnswerEntity
import com.aiteacher.data.local.entity.TestingTaskEntity

/**
 * 知识题目教学数据库 - 存储知识点、题目、学生、教学计划、教学任务和错题记录信息
 */
@Database(
    entities = [KnowledgeEntity::class, QuestionEntity::class, StudentEntity::class, 
               TeachingPlanEntity::class, TeachingTaskEntity::class, WrongAnswerEntity::class,
               TestingTaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudentDatabase : RoomDatabase() {
    
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun questionDao(): QuestionDao
    abstract fun studentDao(): StudentDao
    abstract fun teachingPlanDao(): TeachingPlanDao
    abstract fun teachingTaskDao(): TeachingTaskDao
    abstract fun wrongAnswerDao(): WrongAnswerDao
    abstract fun testingTaskDao(): TestingTaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: StudentDatabase? = null
        
        fun getDatabase(context: Context): StudentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudentDatabase::class.java,
                    "knowledge_question_edu_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}