package com.aiteacher.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.aiteacher.data.local.dao.UserDao
import com.aiteacher.data.local.dao.SessionDao
import com.aiteacher.data.local.dao.MessageDao
import com.aiteacher.data.local.entity.UserEntity
import com.aiteacher.data.local.entity.SessionEntity
import com.aiteacher.data.local.entity.MessageEntity

/**
 * 用户会话消息数据库 - 存储用户、会话和消息信息
 */
@Database(
    entities = [UserEntity::class, SessionEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UserDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null
        
        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_session_message_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}