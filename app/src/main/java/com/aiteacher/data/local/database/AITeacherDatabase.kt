package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.entity.StudentEntity

/**
 * AI教师数据库 - 简化版，只存储学生信息
 */
@Database(
    entities = [StudentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AITeacherDatabase : RoomDatabase() {
    
    abstract fun studentDao(): StudentDao
    
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
