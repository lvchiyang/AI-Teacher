package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.aiteacher.data.local.dao.KnowledgeDao
import com.aiteacher.data.local.entity.KnowledgeEntity

/**
 * 知识数据库 - 存储知识点信息
 */
@Database(
    entities = [KnowledgeEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KnowledgeDatabase : RoomDatabase() {
    
    abstract fun knowledgeDao(): KnowledgeDao
    
    companion object {
        @Volatile
        private var INSTANCE: KnowledgeDatabase? = null
        
        fun getDatabase(context: Context): KnowledgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KnowledgeDatabase::class.java,
                    "knowledge_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}