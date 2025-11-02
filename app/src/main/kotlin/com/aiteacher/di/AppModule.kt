package com.aiteacher.di

import com.aiteacher.ai.service.MemoryManager
import com.aiteacher.data.local.dao.*
import com.aiteacher.data.local.repository.*
import com.aiteacher.data.local.database.AppDatabase
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
import org.koin.core.qualifier.named

/**
 * Koin依赖注入模块
 */
val appModule = module {

    // 数据库相关
    single { AppDatabase.getDatabase(androidContext()) }
    single<StudentDao> { get<AppDatabase>().studentDao() }
    single<TeachingTaskDao> { get<AppDatabase>().teachingTaskDao() }
    single<TestingTaskDao> { get<AppDatabase>().testingTaskDao() }
    single<KnowledgeDao> { get<AppDatabase>().knowledgeDao() }
    single<QuestionDao> { get<AppDatabase>().questionDao() }
    single<UserDao> { get<AppDatabase>().userDao() }
    single<SessionDao> { get<AppDatabase>().sessionDao() }
    single<MessageDao> { get<AppDatabase>().messageDao() }
    single<TeachingPlanDao> { get<AppDatabase>().teachingPlanDao() }

    // 仓库相关
    single<StudentRepository> { StudentRepository(get()) }
    single<TeachingTaskRepository> { TeachingTaskRepository(get()) }
    single<TestingTaskRepository> { TestingTaskRepository(get(), get()) }
    single<QuestionRepository> { QuestionRepository(get()) }
    single<KnowledgeRepository> { KnowledgeRepository(get()) }
    single<UserRepository> { UserRepository(get()) }
    single<SessionRepository> { SessionRepository(get()) }
    single<MessageRepository> { MessageRepository(get()) }
    single<TeachingPlanRepository> { TeachingPlanRepository(get()) }
    
    // Agent相关 - 暂时只实现SecretaryAgent
    single { SecretaryAgent(tools = getAllBuiltinTools()) }

    // 服务相关 - MemoryManager 配置
    // 默认的 MemoryManager
    single(qualifier = named("default")) { MemoryManager() }
    // SecretaryAgent 专用的 MemoryManager
    single(qualifier = named("SecretaryAgent")) { MemoryManager() }
    // HomeAgent 专用的 MemoryManager
    single(qualifier = named("HomeAgent")) { MemoryManager() }
    
    // UseCase
    factory { TeachingOutlineUseCase(get(), get()) }
    factory { StudentUseCase(get()) }
    
    // ViewModel - 使用single让MainViewModel全局共享
    single { MainViewModel(get()) }  // 改为single，让所有界面共享同一个实例（包含对话功能）
    viewModel { TeachingOutlineViewModel(get(), get()) }  // 注入useCase和mainViewModel
    viewModel { LearningViewModel() }
}
