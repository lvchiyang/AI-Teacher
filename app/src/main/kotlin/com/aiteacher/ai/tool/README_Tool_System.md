# Kotlin 工具系统实现

## 概述

本项目实现了仿照 Python 版本 `utils.py` 中 `base_tool` 的 Kotlin 工具系统，为 Android 应用提供灵活的工具定义和使用机制。

## 架构设计

### 核心组件

1. **BaseTool** - 基础工具抽象类
   - 支持工具规格定义和转换
   - 提供工具执行函数设置
   - 支持从规格创建工具实例

2. **ToolManager** - 工具管理器
   - 注册和管理多个工具
   - 提供工具调用接口
   - 支持工具可用性检查

3. **内置工具** - 预定义工具
   - MathTool - 数学计算工具
   - StringTool - 字符串处理工具
   - TimeTool - 时间操作工具

## 文件结构

```
app/src/main/kotlin/com/aiteacher/ai/tool/
├── BaseTool.kt           # 基础工具类和管理器
├── MathTool.kt           # 数学计算工具
├── StringTool.kt         # 字符串处理工具
├── TimeTool.kt           # 时间操作工具
├── ToolTest.kt           # 工具系统测试
├── ToolExample.kt        # 使用示例
└── README_Tool_System.md # 说明文档
```

## 使用方法

### 1. 基本工具使用

```kotlin
// 创建数学工具
val mathTool = MathTool()

// 执行计算
val result = mathTool.toolFunction("add", 5.0, 3.0)
println("5 + 3 = $result")

// 获取工具规格
val spec = mathTool.toToolSpec()
```

### 2. 工具管理器使用

```kotlin
val toolManager = ToolManager()

// 注册工具
toolManager.registerTool(MathTool())
toolManager.registerTool(StringTool())

// 调用工具
val result = toolManager.callTool("math_calculator", "multiply", 6.0, 7.0)

// 检查工具可用性
val isAvailable = toolManager.hasTool("math_calculator")
```

### 3. 自定义工具创建

```kotlin
val customTool = object : BaseTool(
    toolName = "greeting_tool",
    toolDescription = "生成个性化问候语",
    parameters = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "name" to mapOf(
                "type" to "string",
                "description" to "要问候的名字"
            )
        ),
        "required" to listOf("name")
    )
) {
    override suspend fun toolFunction(vararg args: Any): Any {
        val name = args.getOrNull(0) as? String ?: "World"
        return "Hello, $name!"
    }
}
```

### 4. 从规格创建工具

```kotlin
val toolSpec = mapOf(
    "type" to "function",
    "function" to mapOf(
        "name" to "weather_tool",
        "description" to "获取天气信息",
        "parameters" to mapOf(
            "type" to "object",
            "properties" to mapOf(
                "city" to mapOf(
                    "type" to "string",
                    "description" to "城市名称"
                )
            ),
            "required" to listOf("city")
        )
    )
)

val weatherTool = BaseTool.fromSpec(toolSpec) { args ->
    val city = args.getOrNull(0) as? String ?: "Unknown"
    "今天 $city 的天气是晴天，温度 25°C"
}
```

## 内置工具

### MathTool - 数学计算工具

支持基本数学运算：
- `add` - 加法
- `subtract` - 减法
- `multiply` - 乘法
- `divide` - 除法

```kotlin
val mathTool = MathTool()
val result = mathTool.toolFunction("add", 10.0, 5.0) // 结果: 15.0
```

### StringTool - 字符串处理工具

支持字符串操作：
- `uppercase` - 转大写
- `lowercase` - 转小写
- `reverse` - 反转字符串
- `length` - 获取长度

```kotlin
val stringTool = StringTool()
val result = stringTool.toolFunction("uppercase", "hello") // 结果: "HELLO"
```

### TimeTool - 时间操作工具

支持时间相关操作：
- `current_time` - 获取当前时间
- `format_time` - 格式化时间
- `parse_time` - 解析时间

```kotlin
val timeTool = TimeTool()
val result = timeTool.toolFunction("current_time") // 结果: 当前时间字符串
```

## Agent 集成

工具系统已集成到 Agent 系统中：

```kotlin
// 创建带工具的 Agent
val agent = AgentFactory.createChatAgent(
    name = "ToolAgent",
    enableTools = true
)

// Agent 自动拥有数学、字符串、时间工具
println("可用工具: ${agent.toolManager.getToolNames()}")

// 直接调用工具
val result = agent.callTool("math_calculator", "add", 3.0, 4.0)
```

## 测试和示例

### 运行工具测试

```kotlin
// 运行所有工具测试
ToolTest.runAllTests()
```

### 运行使用示例

```kotlin
// 运行所有示例
ToolExample.runAllExamples()
```

### 运行 Agent 工具测试

```kotlin
// 运行 Agent 工具测试
AgentToolTest.runAllTests()
```

## 特性

1. **类型安全** - 使用 Kotlin 的类型系统确保工具调用的安全性
2. **异步支持** - 所有工具函数都支持协程异步执行
3. **灵活扩展** - 可以轻松创建自定义工具
4. **Agent 集成** - 与 Agent 系统无缝集成
5. **规格驱动** - 支持从 JSON 规格创建工具

## 与 Python 版本的对应关系

| Python 版本 | Kotlin 版本 | 说明 |
|-------------|-------------|------|
| `base_tool` | `BaseTool` | 基础工具类 |
| `to_tool_spec()` | `toToolSpec()` | 转换为工具规格 |
| `from_spec()` | `fromSpec()` | 从规格创建工具 |
| `tool_function` | `toolFunction()` | 工具执行函数 |
| `set_function()` | 构造函数参数 | 设置工具函数 |

## 注意事项

1. **异步执行** - 所有工具函数都是 `suspend` 函数，需要在协程中调用
2. **参数类型** - 工具参数使用 `Any` 类型，需要适当的类型转换
3. **错误处理** - 工具调用可能抛出异常，需要适当的错误处理
4. **内存管理** - 工具实例会被 Agent 持有，注意内存使用

## 扩展指南

### 创建新工具

1. 继承 `BaseTool` 类
2. 实现 `toolFunction` 方法
3. 定义工具参数规格
4. 注册到 `ToolManager`

### 集成到 Agent

1. 在 `AgentFactory` 中添加工具
2. 在 Agent 构造函数中传入工具列表
3. Agent 会自动注册和配置工具

这个工具系统提供了与 Python 版本相同的功能，同时充分利用了 Kotlin 的类型安全和协程支持。
