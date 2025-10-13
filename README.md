# AI教师系统项目

## 项目概述

AI教师系统是一个基于人工智能技术的K12在线教育平台，通过多个AI Agent协作，为学生提供个性化的教学服务。系统采用Android应用 + 后端服务 + AI服务的三层架构，实现智能教学、学习检验和家长监督等功能。

## 项目文件结构

```
AI Teacher/
├── README.md                           # 项目说明文档
├── explaination.py                     # AI工具使用示例
├── utils.py                            # AI Agent基础工具类
├── tool_example.json                   # 工具配置示例
├── 初中数学大纲.md                     # 教学大纲知识库
├── __pycache__/                        # Python缓存目录
├── doc/                                # 项目文档目录
│   ├── 01_市场调研报告.md              # 市场调研分析
│   ├── 02_用户需求分析.md              # 用户需求分析
│   ├── 03_技术实现方案.md              # 技术架构设计
│   ├── agent_business_logic.md         # Agent业务逻辑设计
│   ├── 业务流程梳理.md                 # 核心业务流程
│   ├── 详细业务流程梳理.md             # 详细业务流程
│   ├── UI 层 ↔ 业务逻辑层.md           # UI与业务逻辑交互
│   └── 设计文档.md                     # 产品设计文档
└── app/                                # Android应用目录
    ├── build.gradle.kts                # 应用级构建配置
    ├── src/main/                       # 主要源代码
    │   ├── AndroidManifest.xml         # 应用清单文件
    │   ├── java/com/aiteacher/         # Kotlin源代码
    │   │   ├── app/                    # 应用入口
    │   │   │   └── AITeacherApplication.kt
    │   │   ├── data/                   # 数据层
    │   │   │   ├── local/              # 本地数据存储
    │   │   │   │   └── StudentRepositoryImpl.kt
    │   │   │   ├── remote/             # 远程数据接口
    │   │   │   └── repository/         # 数据仓库
    │   │   ├── domain/                 # 领域层
    │   │   │   ├── model/              # 数据模型
    │   │   │   │   ├── Student.kt
    │   │   │   │   ├── TeachingPlan.kt
    │   │   │   │   ├── TeachingTask.kt
    │   │   │   │   ├── TestingTask.kt
    │   │   │   │   └── StudentManager.kt
    │   │   │   └── usecase/            # 业务用例
    │   │   │       ├── StudentUseCase.kt
    │   │   │       ├── TeachingPlanUseCase.kt
    │   │   │       ├── TeachingTaskUseCase.kt
    │   │   │       └── TestingTaskUseCase.kt
    │   │   ├── presentation/           # 表现层
    │   │   │   ├── ui/                 # UI组件
    │   │   │   │   ├── MainActivity.kt
    │   │   │   │   ├── screens/        # 页面组件
    │   │   │   │   │   ├── WelcomeScreen.kt
    │   │   │   │   │   ├── LearningScreen.kt
    │   │   │   │   │   └── ParentDashboardScreen.kt
    │   │   │   │   └── theme/          # 主题样式
    │   │   │   │       ├── Color.kt
    │   │   │   │       ├── Theme.kt
    │   │   │   │       └── Type.kt
    │   │   │   ├── viewmodel/          # 视图模型
    │   │   │   │   └── LearningViewModel.kt
    │   │   │   └── navigation/         # 导航组件
    │   │   │       ├── AITeacherNavigation.kt
    │   │   │       └── Screen.kt
    │   │   ├── di/                     # 依赖注入
    │   │   │   └── AppModule.kt
    │   │   └── utils/                  # 工具类
    │   └── res/                        # 资源文件
    │       ├── layout/                 # 布局文件
    │       ├── values/                 # 值资源
    │       │   ├── strings.xml
    │       │   ├── colors.xml
    │       │   └── themes.xml
    │       ├── drawable/               # 图片资源
    │       └── mipmap-*/               # 应用图标
    ├── src/test/                       # 单元测试
    └── src/androidTest/                # 集成测试
```

## 核心功能

### 1. AI Agent系统
- **教秘Agent**: 负责整体教学计划把控，章节进度决策，向家长实时报告
- **教学Agent**: 细化章节内容，制定教学任务，交互式教学，例题讲解
- **检验Agent**: 出题检验，图片识别作答，错题分析，知识点总结

### 2. 学生功能
- 个性化学习计划
- 交互式AI教学
- 智能题目检验
- 游戏化学习体验
- 成就系统激励

### 3. 家长功能
- 实时学习进度监控
- 详细学习报告
- 异常情况预警
- 学习成果分享

## 技术架构

### Android端
- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构模式**: MVVM
- **导航**: Navigation Compose
- **状态管理**: StateFlow + Compose State
- **网络框架**: Retrofit + OkHttp (计划中)
- **数据库**: Room (SQLite) (计划中)

### 后端服务
- **开发语言**: Java
- **框架**: Spring Boot + Spring Cloud
- **数据库**: MySQL + Redis
- **消息队列**: RabbitMQ
- **文件存储**: MinIO

### AI服务
- **开发语言**: Python
- **AI框架**: 基于utils.py扩展
- **LLM服务**: Qwen API
- **图片识别**: OpenCV + 自定义模型
- **语音处理**: SpeechRecognition + TTS

## 商业模式

### 收费模式
- **基础免费功能**: 基础AI教学、简单题目练习、基础学习报告
- **付费增值服务**: 高级AI教学、家长实时报告、高级题库、一对一辅导

### 定价策略
- **月费**: 29-49元/月
- **年费**: 299-499元/年
- **单次付费**: 特定功能按次收费

## 开发计划

### 第一阶段（已完成）✅
- ✅ Android基础框架搭建
- ✅ MVVM架构实现
- ✅ UI层与业务逻辑层打通
- ✅ MVP核心流程实现（教学计划 → 教学任务 → 检验任务）
- ✅ 核心数据模型（教学计划、教学任务、检验任务）
- ✅ 简化架构（移除复杂依赖注入和数据库）

### 第二阶段（进行中）🔄
- 🔄 Room数据库集成
- 🔄 Retrofit网络层实现
- 🔄 AI服务接口对接
- 🔄 用户认证系统
- 🔄 数据持久化

### 页面路由系统（已完成）✅
- ✅ Navigation Compose导航框架
- ✅ 类型安全的路由定义
- ✅ 参数传递和状态管理
- ✅ 多页面流程支持

### 第三阶段（计划中）📋
- 📋 AI Agent详细实现
- 📋 图片识别功能
- 📋 语音交互功能
- 📋 高级教学算法
- 📋 性能优化

### 第四阶段（计划中）📋
- 📋 后端服务开发
- 📋 云端数据同步
- 📋 安全加固
- 📋 上线准备

### 第五阶段（持续）📋
- 📋 用户反馈收集
- 📋 功能迭代优化
- 📋 新功能开发
- 📋 运营和维护

## 项目特色

1. **AI个性化教学**: 基于AI技术的真正个性化学习体验
2. **游戏化学习**: 将学习过程游戏化，提高学习兴趣
3. **家长深度参与**: 建立家长与孩子的学习共同体
4. **本地化内容**: 针对不同地区教材的精准内容
5. **解耦架构设计**: 教学大纲与学习进度分离，便于维护和扩展
6. **MVP快速迭代**: 先实现核心功能，再逐步完善

## 核心数据模型

### 学生实体 (Student)
```kotlin
data class Student(
    val studentId: String,                    // 学生ID
    val studentName: String,                  // 学生姓名
    val grade: Int,                           // 年级
    val currentChapter: String,               // 当前学习章节
    val learningProgress: LearningProgress    // 学习进度
)
```

### 学习进度 (LearningProgress)
```kotlin
data class LearningProgress(
    val notTaught: List<String>,              // 未讲解的知识点ID列表
    val taughtToReview: List<String>,        // 已讲解待复习的知识点ID列表
    val notMastered: List<String>,            // 未掌握的知识点ID列表
    val basicMastery: List<String>,           // 初步掌握的知识点ID列表
    val fullMastery: List<String>,            // 熟练掌握的知识点ID列表
    val lastUpdateTime: String                // 最后更新时间
)
```

### 掌握状态枚举 (MasteryStatus)
```kotlin
enum class MasteryStatus {
    NOT_TAUGHT,        // 未讲解
    TAUGHT_TO_REVIEW,  // 已讲解待复习
    NOT_MASTERED,      // 未掌握
    BASIC_MASTERY,     // 初步掌握
    FULL_MASTERY       // 熟练掌握
}
```

## MVP流程实现

### 1. 用户打开应用
- 欢迎页面输入学生信息
- 教秘Agent制定今日教学计划
- UI显示教学计划

### 2. 教学阶段
- 教学Agent讲解知识点
- 学生答题验证理解
- 循环教学所有知识点

### 3. 检验阶段
- 教学完成后，检验Agent出题
- 学生答题，AI评判
- 更新学生学习进度

### 4. 家长监督
- 实时学习进度监控
- 详细学习报告
- 学习建议

## 市场前景

AI教师系统具有广阔的市场前景，通过差异化定位和核心功能聚焦，有望在竞争激烈的K12在线教育市场中占据一席之地。预计第一年用户规模达到10万，付费率15%，收入500万元。

## 快速开始

### 环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 11 或更高版本
- Android SDK API 24 或更高版本

### 构建步骤
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 同步Gradle依赖
4. 运行应用

### 项目状态
- ✅ 基础架构完成
- ✅ UI层实现完成
- ✅ MVP流程打通
- 🔄 数据持久化开发中
- 📋 AI服务集成计划中

## 技术栈详情

### 已实现技术
- **Kotlin**: 主要开发语言
- **Jetpack Compose**: 现代UI框架
- **MVVM**: 架构模式
- **Navigation Compose**: 导航框架
- **StateFlow**: 状态管理
- **页面路由系统**: 类型安全的多页面导航

### 计划集成技术
- **Room**: 本地数据库
- **Retrofit**: 网络请求
- **Coroutines**: 异步编程
- **Material Design 3**: UI设计规范

## 联系方式

如有任何问题或建议，请联系项目团队。

---

*最后更新时间: 2025年10月*
