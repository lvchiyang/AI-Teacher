package com.aiteacher.di

import android.content.Context
import androidx.room.Room
import com.aiteacher.data.local.database.AITeacherDatabase
import com.aiteacher.data.local.repository.StudentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AITeacherDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AITeacherDatabase::class.java,
            "ai_teacher_database"
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideStudentRepository(database: AITeacherDatabase): StudentRepository {
        return StudentRepository(database.studentDao())
    }
}
