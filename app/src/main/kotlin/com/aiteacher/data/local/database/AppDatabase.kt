package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.aiteacher.data.local.dao.*
import com.aiteacher.data.local.entity.*

/**
 * Room数据库
 */
@Database(
    entities = [
        StudentEntity::class,
        TeachingTaskEntity::class,
        TestingTaskEntity::class,
        TeachingPlanEntity::class,
        QuestionEntity::class,
        KnowledgeEntity::class,
        UserEntity::class,
        SessionEntity::class,
        MessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun teachingTaskDao(): TeachingTaskDao
    abstract fun testingTaskDao(): TestingTaskDao
    abstract fun teachingPlanDao(): TeachingPlanDao
    abstract fun questionDao(): QuestionDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_teacher_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}