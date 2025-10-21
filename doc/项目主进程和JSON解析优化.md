# AI教师系统 - 项目主进程和JSON解析优化

## 项目主进程

**主进程文件**：`app/src/main/java/com/aiteacher/presentation/ui/MainActivity.kt`

这是Android应用的入口点，在AndroidManifest.xml中定义为启动Activity：

```xml
<activity
    android:name=".presentation.ui.MainActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:theme="@style/Theme.AITeacher">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

## JSON解析优化

### 优化前的问题
1. **混合解析方式**：工具调用使用kotlinx-serialization，教学大纲使用正则表达式
2. **容易出错**：手动正则表达式解析容易出错，不够健壮
3. **维护困难**：不同解析方式增加了代码复杂度

### 优化后的改进
1. **统一解析方式**：全部使用kotlinx-serialization
2. **类型安全**：使用@Serializable注解确保类型安全
3. **错误处理**：更好的异常处理和默认值机制

### 具体实现

#### 1. 创建JSON数据类
```kotlin
@Serializable
data class TeachingPlanJson(
    val reviewKnowledgePoints: List<String>,
    val newKnowledgePoints: List<String>,
    val estimatedDuration: Int,
    val teachingSequence: List<String>
)
```

#### 2. 使用kotlinx-serialization解析
```kotlin
private fun parseTeachingPlan(response: String): TeachingPlanResult {
    return try {
        val jsonStart = response.indexOf('{')
        val jsonEnd = response.lastIndexOf('}') + 1
        
        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            val jsonStr = response.substring(jsonStart, jsonEnd)
            
            // 使用kotlinx-serialization解析JSON
            val json = Json { ignoreUnknownKeys = true }
            val planJson = json.decodeFromString<TeachingPlanJson>(jsonStr)
            
            TeachingPlanResult(
                reviewKnowledgePoints = planJson.reviewKnowledgePoints,
                newKnowledgePoints = planJson.newKnowledgePoints,
                estimatedDuration = planJson.estimatedDuration,
                planDescription = planJson.teachingSequence.joinToString(" -> ")
            )
        } else {
            getDefaultTeachingPlan()
        }
    } catch (e: Exception) {
        println("Error parsing teaching plan JSON: ${e.message}")
        getDefaultTeachingPlan()
    }
}
```

#### 3. 移除冗余代码
- 删除了手动正则表达式解析方法
- 统一了错误处理机制
- 简化了代码结构

## 优势

1. **类型安全**：编译时检查JSON结构
2. **健壮性**：更好的错误处理
3. **可维护性**：统一的解析方式
4. **性能**：kotlinx-serialization性能更好
5. **可扩展性**：易于添加新的JSON字段

## 依赖

项目使用以下JSON序列化依赖：
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
```

这个优化确保了整个项目使用统一的JSON解析方式，提高了代码质量和可维护性。
