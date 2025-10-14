package com.aiteacher.domain.model

/**
 * 检验任务模型
 * 检验Agent执行的检验任务
 */
data class TestingTask(
    val taskId: String,                       // 任务ID
    val studentId: String,                    // 学生ID
    val knowledgePointIds: List<String>,      // 知识点ID列表
    val questions: List<TestQuestion>,        // 题目列表
    val status: TaskStatus,                   // 任务状态
    val currentQuestionIndex: Int,            // 当前题目索引
    val startTime: String,                    // 开始时间
    val timeLimit: Int                        // 时间限制（分钟）
)

/**
 * 测试题目模型
 */
data class TestQuestion(
    val questionId: String,                   // 题目ID
    val knowledgePointId: String,             // 知识点ID
    val content: String,                      // 题目内容
    val image: String?,                       // 题目图片URL
    val type: QuestionType,                   // 题目类型
    val correctAnswer: String,                // 正确答案
    val explanation: String,                   // 解析
    val points: Int,                          // 分值
    val timeLimit: Int                        // 时间限制（分钟）
)

/**
 * 学生答案模型
 */
data class StudentAnswer(
    val answerId: String,                     // 答案ID
    val questionId: String,                   // 题目ID
    val studentId: String,                    // 学生ID
    val answer: String,                       // 学生答案
    val imageAnswer: String?,                 // 图片答案URL
    val isCorrect: Boolean?,                  // 是否正确
    val score: Int,                           // 得分
    val timeSpent: Int,                       // 用时（秒）
    val timestamp: String                     // 提交时间
)

/**
 * 测试结果模型
 */
data class TestingResult(
    val resultId: String,                     // 结果ID
    val taskId: String,                        // 任务ID
    val studentId: String,                    // 学生ID
    val totalScore: Int,                      // 总分
    val maxScore: Int,                         // 满分
    val correctCount: Int,                    // 正确题数
    val totalCount: Int,                      // 总题数
    val timeSpent: Int,                        // 总用时（秒）
    val answers: List<StudentAnswer>,          // 答案列表
    val timestamp: String                      // 完成时间
)