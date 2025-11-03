package com.aiteacher.ai.tool

import com.aiteacher.domain.model.Question

/**
 * 评分工具 - 用于批改学生作答并计算成绩
 */
class GradingTool : BaseTool(
    toolName = "grade_student_answers",
    toolDescription = "批改学生作答并计算成绩（满分100分）",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "questions" to mapOf(
                "type" to "array",
                "items" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "questionId" to mapOf("type" to "string"),
                        "questionText" to mapOf("type" to "string"),
                        "answer" to mapOf("type" to "string"),
                        "questionType" to mapOf("type" to "string"),
                        "difficulty" to mapOf("type" to "integer")
                    ),
                    "required" to listOf("questionId", "questionText", "answer", "questionType")
                ),
                "description" to "题目列表"
            ),
            "studentAnswers" to mapOf(
                "type" to "array",
                "items" to mapOf("type" to "string"),
                "description" to "学生作答列表"
            )
        ),
        "required" to listOf("questions", "studentAnswers")
    )
) {
    
    override suspend fun toolFunction(vararg args: Any): ToolResult {
        try {
            val params = args[0] as? Map<String, Any> ?: return ToolResult.QueryResult("参数错误")
            
            @Suppress("UNCHECKED_CAST")
            val questionsList = params["questions"] as? List<Map<String, Any>> ?: return ToolResult.QueryResult("缺少题目列表")
            
            @Suppress("UNCHECKED_CAST")
            val studentAnswers = params["studentAnswers"] as? List<String> ?: return ToolResult.QueryResult("缺少学生作答列表")
            
            // 转换题目列表
            val questions = questionsList.map { q ->
                Question(
                    questionId = q["questionId"] as? String ?: "",
                    subject = q["subject"] as? String ?: "",
                    grade = (q["grade"] as? Int) ?: 0,
                    questionText = q["questionText"] as? String ?: "",
                    answer = q["answer"] as? String ?: "",
                    questionType = q["questionType"] as? String ?: "",
                    difficulty = q["difficulty"] as? Int,
                    relatedKnowledgeIds = (q["relatedKnowledgeIds"] as? List<String>) ?: emptyList()
                )
            }
            
            // 批改学生作答
            val gradingResult = gradeAnswers(questions, studentAnswers)
            
            return ToolResult.QueryResult(gradingResult)
        } catch (e: Exception) {
            return ToolResult.ExecuteResult.failure("批改作答时发生错误: ${e.message}")
        }
    }
    
    /**
     * 批改学生作答
     * @param questions 题目列表
     * @param studentAnswers 学生作答列表
     * @return 批改结果
     */
    private fun gradeAnswers(questions: List<Question>, studentAnswers: List<String>): Map<String, Any> {
        if (questions.size != studentAnswers.size) {
            return mapOf(
                "error" to "题目数量与作答数量不匹配",
                "score" to 0,
                "totalScore" to 100,
                "feedback" to "无法批改：题目数量与作答数量不匹配"
            )
        }
        
        var totalScore = 0
        var maxScore = 0
        val detailedResults = mutableListOf<Map<String, Any>>()
        
        // 为每道题评分
        questions.zip(studentAnswers).forEach { (question, answer) ->
            val questionMaxScore = (question.difficulty ?: 1) * 10 // 假设每级难度10分
            maxScore += questionMaxScore
            
            val (isCorrect, partialScore) = checkAnswer(question, answer)
            val questionScore = if (isCorrect) questionMaxScore else partialScore
            
            totalScore += questionScore
            
            detailedResults.add(
                mapOf(
                    "questionId" to question.questionId,
                    "questionText" to question.questionText,
                    "correctAnswer" to question.answer,
                    "studentAnswer" to answer,
                    "isCorrect" to isCorrect,
                    "score" to questionScore,
                    "maxScore" to questionMaxScore
                )
            )
        }
        
        // 计算百分制成绩
        val percentageScore = if (maxScore > 0) {
            (totalScore.toDouble() / maxScore * 100).toInt()
        } else {
            0
        }
        
        return mapOf(
            "score" to percentageScore,
            "totalScore" to 100,
            "rawScore" to totalScore,
            "maxRawScore" to maxScore,
            "detailedResults" to detailedResults,
            "feedback" to generateFeedback(percentageScore)
        )
    }
    
    /**
     * 检查单个题目的答案
     * @param question 题目
     * @param studentAnswer 学生作答
     * @return Pair(是否完全正确, 部分得分)
     */
    private fun checkAnswer(question: Question, studentAnswer: String): Pair<Boolean, Int> {
        val correctAnswer = question.answer.trim()
        val studentAns = studentAnswer.trim()
        
        return when (question.questionType) {
            "选择题" -> {
                if (studentAns.equals(correctAnswer, ignoreCase = true)) {
                    Pair(true, 0)
                } else {
                    Pair(false, 0)
                }
            }
            "判断题" -> {
                if (studentAns.equals(correctAnswer, ignoreCase = true)) {
                    Pair(true, 0)
                } else {
                    Pair(false, 0)
                }
            }
            "填空题", "计算题", "简答题" -> {
                // 简单的包含检查，实际应用中可能需要更复杂的逻辑
                if (studentAns.contains(correctAnswer, ignoreCase = true) || 
                    correctAnswer.contains(studentAns, ignoreCase = true)) {
                    Pair(true, 0)
                } else {
                    // 部分匹配给部分分数
                    val similarity = calculateSimilarity(correctAnswer, studentAns)
                    val partialScore = (similarity * 0.5 * (question.difficulty ?: 1) * 10).toInt()
                    Pair(false, partialScore)
                }
            }
            else -> {
                // 默认检查
                if (studentAns.equals(correctAnswer, ignoreCase = true)) {
                    Pair(true, 0)
                } else {
                    Pair(false, 0)
                }
            }
        }
    }
    
    /**
     * 简单的文本相似度计算（实际应用中可以使用更复杂的算法）
     */
    private fun calculateSimilarity(text1: String, text2: String): Double {
        if (text1.isEmpty() && text2.isEmpty()) return 1.0
        if (text1.isEmpty() || text2.isEmpty()) return 0.0
        
        // 简单的字符重叠率计算
        val set1 = text1.toLowerCase().toSet()
        val set2 = text2.toLowerCase().toSet()
        val intersection = set1.intersect(set2).size
        val union = set1.union(set2).size
        
        return if (union == 0) 0.0 else intersection.toDouble() / union
    }
    
    /**
     * 生成反馈意见
     */
    private fun generateFeedback(score: Int): String {
        return when {
            score >= 90 -> "优秀！你对知识点掌握得很好。"
            score >= 80 -> "良好！你对知识点基本掌握，稍加复习会更好。"
            score >= 70 -> "中等！你对知识点有一定掌握，但还需要加强练习。"
            score >= 60 -> "及格！你对知识点掌握一般，需要认真复习。"
            else -> "不及格！你需要重新学习相关知识点。"
        }
    }
}