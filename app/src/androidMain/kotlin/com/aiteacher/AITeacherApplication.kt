package com.aiteacher

import com.aiteacher.data.local.repository.StudentRepository
import org.jetbrains.exposed.sql.Database

/**
 * JVM版本的应用程序类
 * 管理依赖注入和数据库连接
 */
class AITeacherApplication {
    
    // 数据库连接
    private val database by lazy { 
        Database.connect("jdbc:sqlite:ai_teacher.db", driver = "org.sqlite.JDBC")
    }
    
    // 简单的单例依赖管理
    val studentRepository by lazy { StudentRepository(database) }
    
    companion object {
        @Volatile
        private var INSTANCE: AITeacherApplication? = null
        
        fun getInstance(): AITeacherApplication {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AITeacherApplication().also { INSTANCE = it }
            }
        }
    }
}
