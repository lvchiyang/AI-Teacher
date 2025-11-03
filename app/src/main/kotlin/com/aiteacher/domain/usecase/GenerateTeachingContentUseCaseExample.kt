package com.aiteacher.domain.usecase

/**
 * GenerateTeachingContentUseCase 使用示例
 */
class GenerateTeachingContentUseCaseExample {
    // 这个类仅用于演示如何使用GenerateTeachingContentUseCase
    
    /*
    suspend fun exampleUsage() {
        // 创建UseCase实例（依赖项将通过Koin自动注入）
        val generateTeachingContentUseCase = GenerateTeachingContentUseCase()
        
        // 示例1: 生成教学内容
        val params = GenerateTeachingContentUseCase.Params(
            planId = "TP-1234567890"
        )
        
        val result = generateTeachingContentUseCase(params)
        
        if (result.isSuccess) {
            val teachingContent = result.getOrNull()
            println("生成的教学内容: $teachingContent")
        } else {
            println("生成教学内容失败: ${result.exceptionOrNull()?.message}")
        }
        
        // 示例2: 继续与教师代理进行教学对话
        val userInput = "我不太理解这个概念，能再解释一下吗？"
        val continueResult = generateTeachingContentUseCase.continueTeachingSession(userInput)
        
        if (continueResult.isSuccess) {
            val response = continueResult.getOrNull()
            println("教师代理回复: $response")
        } else {
            println("继续教学对话失败: ${continueResult.exceptionOrNull()?.message}")
        }
    }
    */
}