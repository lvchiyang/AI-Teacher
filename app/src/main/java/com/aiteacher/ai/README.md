# Kotlin Agent系统实现

## 概述

本项目实现了基于Kotlin的AI Agent系统，**完全重构**以对应Python版本的`utils.py`和`multi_agent_system.py`，为Android应用提供AI教学功能。

## 架构设计

### 核心组件

1. **BaseAgent** - 智能体基础类（完全对应Python的base_agent）
   - 使用LLMModel作为核心推理模型
   - 支持外围工具注册与调用（BaseTool）
   - 实现单次运行和循环运行逻辑
   - 支持状态机管理（INITIALIZING, RUNNING, PAUSED, STOPPING, STOPPED, ERROR）

2. **BaseTool** - 基础工具类（完全对应Python的base_tool）
   - 支持工具规格定义和转换
   - 提供工具函数设置和执行
   - 支持从规格创建工具实例

3. **LLMModel** - LLM模型类（完全对应Python的llm_model）
   - 支持多种模型参数配置
   - 提供工具添加和解析功能
   - 支持文本生成和工具调用解析

4. **ContextMemory** - 上下文记忆（完全对应Python的ContextMemory）
   - 支持记忆条目的增删改查
   - 提供记忆搜索和上下文获取
   - 支持记忆索引管理

5. **UserManager** - 用户管理（完全对应Python的UserManager）
   - 支持多用户记忆库管理
   - 提供用户切换和记忆持久化
   - 支持自动用户创建

6. **SecretaryAgent** - 教秘Agent
   - 负责制定教学计划（核心能力，通过LLM直接完成）
   - 使用KnowledgeBaseTool检索教学大纲
   - 分析学生学习进度，生成个性化教学计划

## 文件结构

```
app/src/main/java/com/aiteacher/ai/
├── agent/
│   ├── BaseAgent.kt              # 完全重构的基础Agent类
│   ├── SecretaryAgent.kt         # 重构后的教秘Agent（使用知识库工具）
│   └── SecretaryAgentTest.kt     # 教秘Agent测试文件
└── service/
    └── LLMService.kt          # LLM服务接口和实现
```

## 使用方式

### 1. 创建Agent实例

```kotlin
val secretaryAgent = SecretaryAgent()
```

### 2. 制定教学计划（Agent核心能力）

```kotlin
val result = secretaryAgent.createTeachingPlan(
    studentId = "student_001",
    grade = 7,
    currentChapter = "第一章 有理数",
    learningProgress = learningProgress
)

if (result.isSuccess) {
    val plan = result.getOrNull()
    // 使用教学计划
}
```

### 3. 使用知识库工具

```kotlin
// 检索教学大纲
val syllabus = secretaryAgent.callTool("knowledge_base", mapOf(
    "type" to "syllabus",
    "grade" to 7,
    "subject" to "数学"
))

// 搜索知识点
val knowledgePoints = secretaryAgent.callTool("knowledge_base", mapOf(
    "type" to "knowledge_point",
    "grade" to 7,
    "keyword" to "有理数"
))
```

### 4. 单次运行

```kotlin
val response = secretaryAgent.runOnce("请使用knowledge_base工具检索教学大纲，然后制定教学计划")
```

## 界面集成

### 教学大纲界面

新增了`TeachingOutlineScreen`界面，用于显示AI生成的教学大纲：

- **加载状态**: 显示"正在生成本节课教学大纲..."
- **错误处理**: 显示错误信息和重试按钮
- **计划展示**: 显示学生信息、复习知识点、新学知识点
- **操作按钮**: 开始学习、返回主页

### 导航流程

```
主页 → 教学大纲界面 → 学习界面
```

## 与Python版本的对比

| 功能 | Python版本 | Kotlin版本 | 对应关系 |
|------|------------|------------|----------|
| base_agent类 | ✅ | ✅ | 完全对应 |
| base_tool类 | ✅ | ✅ | 完全对应 |
| llm_model类 | ✅ | ✅ | 完全对应 |
| ContextMemory类 | ✅ | ✅ | 完全对应 |
| UserManager类 | ✅ | ✅ | 完全对应 |
| run_once方法 | ✅ | ✅ | 完全对应 |
| run_loop方法 | ✅ | ✅ | 完全对应 |
| 工具调用机制 | ✅ | ✅ | 完全对应 |
| 记忆管理机制 | ✅ | ✅ | 完全对应 |
| 教秘Agent | ✅ | ✅ | 完全对应 |
| 教学Agent | ✅ | 🔄 待实现 |
| 检验Agent | ✅ | 🔄 待实现 |
| 家长Agent | ✅ | 🔄 待实现 |

## 扩展计划

1. **实现其他Agent**
   - TeachingAgent - 教学Agent
   - TestingAgent - 检验Agent
   - ParentAgent - 家长Agent

2. **集成真实LLM服务**
   - 替换MockLLMService
   - 集成Qwen API或其他LLM服务

3. **增强工具系统**
   - 实现更多教学工具
   - 支持图片识别和批改

4. **优化用户体验**
   - 添加更多加载动画
   - 改进错误处理
   - 支持离线模式

## 测试

运行`SecretaryAgentTest.kt`可以测试重构后的教秘Agent：

```kotlin
// 测试制定教学计划（Agent核心能力）
val result = secretaryAgent.createTeachingPlan(...)

// 测试知识库工具调用
val syllabus = secretaryAgent.callTool("knowledge_base", parameters)

// 测试知识点搜索
val searchResult = secretaryAgent.callTool("knowledge_base", searchParams)
```

## 注意事项

1. **制定教学计划是Agent的核心能力**，不是工具，通过LLM直接完成
2. **KnowledgeBaseTool**用于检索教学大纲和知识点信息
3. 当前使用模拟LLM响应，需要后续集成真实的LLM API
4. 工具调用解析功能需要进一步完善
5. 知识库内容基于初中数学大纲.md文档

## 下一步

1. 实现TeachingAgent和TestingAgent
2. 集成真实的LLM服务
3. 完善工具调用解析
4. 添加更多测试用例
5. 优化性能和用户体验
