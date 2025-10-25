package com.aiteacher.di

import com.aiteacher.data.local.database.AppDatabase
import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.ai.agent.SecretaryAgent
import com.aiteacher.ai.tool.getAllBuiltinTools
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.aiteacher.presentation.viewmodel.MainViewModel

/**
 * Koin依赖注入模块
 */
val appModule = module {
    
    // 数据库相关
    single { AppDatabase.getDatabase(androidContext()) }
    single<StudentDao> { get<AppDatabase>().studentDao() }
    single<StudentRepository> { StudentRepository(get()) }
    
    // Agent相关 - 暂时只实现SecretaryAgent
    single { SecretaryAgent(tools = getAllBuiltinTools()) }
    
    // ViewModel
    viewModel { MainViewModel(get()) }
}
