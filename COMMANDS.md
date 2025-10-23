# AITeacher 项目常用指令文档

## 📋 项目概述
这是一个基于 Kotlin Multiplatform 的 AI 教师应用，支持桌面和 Android 平台。

## 🚀 基础构建指令

### 清理项目
```bash
.\gradlew.bat clean
```

### 构建项目
```bash
# 构建所有平台
.\gradlew.bat build

# 仅构建桌面版本
.\gradlew.bat desktopJar

# 仅构建Android版本
.\gradlew.bat assembleDebug
```

### 运行应用
```bash
# 运行桌面应用
.\gradlew.bat run

# 运行Android应用（需要连接设备或启动模拟器）
.\gradlew.bat installDebug
```

## 🔧 开发调试指令

### 查看项目状态
```bash
# 查看Gradle守护进程状态
.\gradlew.bat --status

# 查看项目依赖
.\gradlew.bat dependencies

# 查看项目任务
.\gradlew.bat tasks
```

### 清理和重建
```bash
# 清理所有构建缓存
.\gradlew.bat clean

# 强制重新构建
.\gradlew.bat build --rerun-tasks

# 清理Gradle缓存
.\gradlew.bat clean --refresh-dependencies
```

## 📱 Android 相关指令

### Android 构建
```bash
# 构建Debug版本
.\gradlew.bat assembleDebug

# 构建Release版本
.\gradlew.bat assembleRelease

# 安装到设备
.\gradlew.bat installDebug

# 卸载应用
.\gradlew.bat uninstallDebug
```

### Android 测试
```bash
# 运行单元测试
.\gradlew.bat testDebugUnitTest

# 运行Android测试
.\gradlew.bat connectedAndroidTest
```

## 🖥️ 桌面应用相关指令

### 桌面构建和运行
```bash
# 构建桌面JAR
.\gradlew.bat desktopJar

# 运行桌面应用
.\gradlew.bat run

# 创建可执行JAR
.\gradlew.bat createDistributable
```

## 🔍 调试和诊断指令

### 查看构建信息
```bash
# 查看详细构建信息
.\gradlew.bat build --info

# 查看调试信息
.\gradlew.bat build --debug

# 查看堆栈跟踪
.\gradlew.bat build --stacktrace
```

### 依赖管理
```bash
# 查看依赖树
.\gradlew.bat dependencies

# 查看过时依赖
.\gradlew.bat dependencyUpdates

# 刷新依赖
.\gradlew.bat build --refresh-dependencies
```

## 🧪 测试相关指令

### 运行测试
```bash
# 运行所有测试
.\gradlew.bat test

# 运行桌面测试
.\gradlew.bat desktopTest

# 运行Android单元测试
.\gradlew.bat testDebugUnitTest
```

## 📦 打包和分发

### 创建分发包
```bash
# 创建桌面应用分发包
.\gradlew.bat createDistributable

# 创建Android APK
.\gradlew.bat assembleRelease
```

## 🛠️ 开发工具指令

### IDE 支持
```bash
# 生成IDE项目文件
.\gradlew.bat idea

# 生成Eclipse项目文件
.\gradlew.bat eclipse
```

### 代码质量
```bash
# 运行Lint检查
.\gradlew.bat lintDebug

# 生成Lint报告
.\gradlew.bat lintReportDebug
```

## 🚨 故障排除指令

### 常见问题解决
```bash
# 清理Gradle守护进程
.\gradlew.bat --stop

# 重新启动守护进程
.\gradlew.bat --daemon

# 查看守护进程状态
.\gradlew.bat --status

# 强制刷新依赖
.\gradlew.bat build --refresh-dependencies
```

### 环境检查
```bash
# 检查Java版本
java -version

# 检查Gradle版本
.\gradlew.bat --version

# 检查Android SDK
.\gradlew.bat androidDependencies
```

## 📋 项目特定指令

### MCP 服务相关
```bash
# 运行MCP演示
.\gradlew.bat run
# 然后在UI界面点击"启动MCP服务"按钮

# 查看MCP服务状态
# 在控制台查看以下输出：
# "Simple MCP Server started with tools: add, subtract, multiply, divide"
# "✅ MCP Server is running correctly!"
```

### 数据库相关
```bash
# 初始化数据库（首次运行）
.\gradlew.bat run
# 应用会自动创建SQLite数据库
```

## 🔄 常用工作流程

### 日常开发流程
```bash
# 1. 清理项目
.\gradlew.bat clean

# 2. 构建项目
.\gradlew.bat build

# 3. 运行桌面应用
.\gradlew.bat run
```

### 发布准备流程
```bash
# 1. 清理项目
.\gradlew.bat clean

# 2. 运行所有测试
.\gradlew.bat test

# 3. 构建Release版本
.\gradlew.bat assembleRelease

# 4. 创建桌面分发包
.\gradlew.bat createDistributable
```

## 📝 注意事项

1. **首次运行**：确保已设置 `JAVA_HOME` 环境变量
2. **Android开发**：需要安装Android SDK和配置环境变量
3. **网络问题**：如遇到依赖下载问题，可使用 `--refresh-dependencies` 参数
4. **内存不足**：如遇到内存问题，可增加Gradle JVM参数

## 🆘 紧急情况

### 完全重置项目
```bash
# 停止所有Gradle进程
.\gradlew.bat --stop

# 删除构建缓存
rmdir /s build
rmdir /s .gradle

# 清理项目
.\gradlew.bat clean

# 重新构建
.\gradlew.bat build
```

---

**最后更新**: 2024年12月
**项目版本**: 1.0.0
**支持的平台**: Windows, Android
