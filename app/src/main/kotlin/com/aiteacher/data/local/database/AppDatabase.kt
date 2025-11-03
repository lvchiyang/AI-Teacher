package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.aiteacher.data.local.dao.*
import com.aiteacher.data.local.entity.*
import java.io.File

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
    version = 2,
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
        private const val DATABASE_NAME = "ai_teacher_database"
        private const val PREFS_NAME = "database_prefs"
        private const val KEY_DATABASE_VERSION = "database_version"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 删除数据库文件
         * 包括主数据库文件和相关的 WAL (Write-Ahead Logging) 文件
         */
        fun deleteDatabase(context: Context): Boolean {
            return try {
                val dbPath = context.getDatabasePath(DATABASE_NAME)
                val dbDir = dbPath.parent
                
                // 关闭现有实例
                INSTANCE?.close()
                INSTANCE = null
                
                // 删除数据库相关文件
                val files = listOf(
                    dbPath,
                    File(dbDir, "$DATABASE_NAME-shm"),  // Shared memory file
                    File(dbDir, "$DATABASE_NAME-wal")   // Write-ahead log file
                )
                
                var deleted = true
                files.forEach { file ->
                    if (file.exists()) {
                        deleted = deleted && file.delete()
                        Log.d("AppDatabase", "删除数据库文件: ${file.path}, 成功: ${file.exists().not()}")
                    }
                }
                
                // 清除版本记录
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().remove(KEY_DATABASE_VERSION).apply()
                
                Log.d("AppDatabase", "数据库删除完成")
                deleted
            } catch (e: Exception) {
                Log.e("AppDatabase", "删除数据库失败", e)
                false
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // 检查数据库版本，如果版本变化则删除旧数据库
                checkAndMigrateDatabase(context)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 检查数据库版本，如果版本变化则删除旧数据库
         */
        private fun checkAndMigrateDatabase(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // 从 @Database 注解中读取当前版本号
            val currentDbVersion = getDatabaseVersion()
            val savedVersion = prefs.getInt(KEY_DATABASE_VERSION, -1)
            
            if (savedVersion != -1 && savedVersion != currentDbVersion) {
                // 版本不匹配，删除旧数据库
                Log.d("AppDatabase", "检测到数据库版本变化: $savedVersion -> $currentDbVersion, 删除旧数据库")
                deleteDatabase(context)
            }
            
            // 保存当前版本
            prefs.edit().putInt(KEY_DATABASE_VERSION, currentDbVersion).apply()
        }
        
        /**
         * 从 @Database 注解中获取数据库版本号
         */
        private fun getDatabaseVersion(): Int {
            return try {
                val annotation = AppDatabase::class.java.getAnnotation(Database::class.java)
                annotation?.version ?: 1
            } catch (e: Exception) {
                Log.e("AppDatabase", "无法读取数据库版本，使用默认值 1", e)
                1
            }
        }
    }
}