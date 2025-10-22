package com.aiteacher.ai.service

/**
 * LLM输出数据类
 */
data class LLMOutput(
    val content: String,
    val model: String,
    val usage: Map<String, Any>?
)
