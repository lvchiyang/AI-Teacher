package com.aiteacher.di

import com.aiteacher.data.local.database.AppDatabase
import com.aiteacher.data.local.dao.StudentDao
import com.aiteacher.data.local.repository.StudentRepository
import com.aiteacher.ai.agent.SecretaryAgent
import com.aiteacher.ai.tool.getAllBuiltinTools
import com.aiteacher.domain.usecase.TeachingOutlineUseCase
import com.aiteacher.domain.usecase.StudentUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.aiteacher.presentation.viewmodel.MainViewModel
import com.aiteacher.presentation.viewmodel.TeachingOutlineViewModel
import com.aiteacher.presentation.viewmodel.LearningViewModel

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
    
    // UseCase
    factory { TeachingOutlineUseCase(get(), get()) }
    factory { StudentUseCase(get()) }
    
    // ViewModel - 使用single让MainViewModel全局共享
    single { MainViewModel(get()) }  // 改为single，让所有界面共享同一个实例
    viewModel { TeachingOutlineViewModel(get(), get()) }  // 注入useCase和mainViewModel
    viewModel { LearningViewModel() }
}
