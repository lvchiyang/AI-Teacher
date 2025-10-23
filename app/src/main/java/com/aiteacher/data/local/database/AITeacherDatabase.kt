package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.dao.SessionDao
import com.aiteacher.data.local.dao.MessageDao
import com.aiteacher.data.local.dao.WrongAnswerDao
import com.aiteacher.data.local.dao.TeachingPlanDao
import com.aiteacher.data.local.dao.TeachingTaskDao
import com.aiteacher.data.local.entity.StudentEntity
import com.aiteacher.data.local.entity.SessionEntity
import com.aiteacher.data.local.entity.MessageEntity
import com.aiteacher.data.local.entity.WrongAnswerEntity
import com.aiteacher.data.local.entity.TeachingPlanEntity
import com.aiteacher.data.local.entity.TeachingTaskEntity

/**
 * AI教师数据库 - 存储学生信息、会话信息、消息信息、错题记录和教学计划信息
 */
@Database(
    entities = [StudentEntity::class, SessionEntity::class, MessageEntity::class, 
               WrongAnswerEntity::class, TeachingPlanEntity::class, TeachingTaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AITeacherDatabase : RoomDatabase() {
    
    abstract fun studentDao(): StudentDao
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    abstract fun wrongAnswerDao(): WrongAnswerDao
    abstract fun teachingPlanDao(): TeachingPlanDao
    abstract fun teachingTaskDao(): TeachingTaskDao
    
    companion object {
        @Volatile
        private var INSTANCE: AITeacherDatabase? = null
        
        fun getDatabase(context: Context): AITeacherDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AITeacherDatabase::class.java,
                    "ai_teacher_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}