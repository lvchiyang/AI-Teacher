package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.aiteacher.data.local.dao.QuestionDao
import com.aiteacher.data.local.entity.QuestionEntity

/**
 * 题目数据库 - 存储题目信息
 */
@Database(
    entities = [QuestionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuestionDatabase : RoomDatabase() {
    
    abstract fun questionDao(): QuestionDao
    
    companion object {
        @Volatile
        private var INSTANCE: QuestionDatabase? = null
        
        fun getDatabase(context: Context): QuestionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuestionDatabase::class.java,
                    "question_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}