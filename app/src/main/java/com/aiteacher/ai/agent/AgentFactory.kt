package com.aiteacher.ai.agent

import com.aiteacher.ai.service.LLMModel

/**
 * Agent工厂 - 提供便捷的Agent创建方式
 * 简化设计，避免功能重叠
 */
object AgentFactory {
    
    /**
     * 创建教秘Agent
     */
    fun createSecretaryAgent(
        model: LLMModel = LLMModel("qwen-max")
    ): SecretaryAgent {
        return SecretaryAgent(model)
    }
    
    /**
     * 创建教学Agent
     */
    fun createTeachingAgent(
        model: LLMModel = LLMModel("qwen-max")
    ): TeachingAgent {
        return TeachingAgent(model)
    }
    
    /**
     * 创建纯对话Agent
     */
    fun createChatAgent(
        name: String = "ChatAgent",
        model: LLMModel = LLMModel("qwen-max")
    ): BaseAgent {
        return object : BaseAgent(
            name = name,
            description = "纯对话智能体，不使用任何外部工具",
            model = model,
            mcpConfigPath = null
        ) {
            override fun buildSystemPrompt(): String {
                return """你是$name，一个专注于对话交流的智能助手。
你不使用任何外部工具，完全依靠自己的知识和推理能力来帮助用户。
请提供有用、准确、友好的回答。"""
            }
        }
    }
}

/**
 * TeachingAgent - 教学代理
 */
class TeachingAgent(
    model: LLMModel = LLMModel("qwen-max")
) : BaseAgent(
    name = "TeachingAgent",
    description = "教学代理，负责具体教学实施",
    model = model,
    mcpConfigPath = null
) {
    
    override fun buildSystemPrompt(): String {
        return """
            你是TeachingAgent，负责具体教学实施。
            
            你的主要职责：
            1. 根据教学计划进行具体教学
            2. 生成练习题和测试题
            3. 评估学生学习效果
            4. 提供个性化学习建议
            
            教学实施流程：
            1. 根据教学计划确定当前教学内容
            2. 生成适合的练习题
            3. 评估学生答题情况
            4. 提供反馈和改进建议
            
            请根据教学计划，提供具体的教学实施建议。
        """.trimIndent()
    }
}