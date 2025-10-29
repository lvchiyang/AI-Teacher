# Android APK 打包指南

本指南将帮助您将 AI Teacher 应用打包成 APK 文件，以便在 Android 设备上安装和运行。

## 目录

1. [准备工作](#准备工作)
2. [构建 Debug APK](#构建-debug-apk)
3. [构建 Release APK](#构建-release-apk)
4. [安装 APK 到设备](#安装-apk-到设备)
5. [常见问题](#常见问题)

---

## 准备工作

### 1. 环境要求

- Android Studio 已安装
- Java JDK 17 或更高版本
- Android SDK（通过 Android Studio 安装）
- Gradle（项目会自动下载）

### 2. 检查项目配置

确保以下文件已正确配置：

#### `app/build.gradle.kts`

检查签名配置：
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../aiteacher-release.keystore")
        storePassword = "aiteacher123456"
        keyAlias = "aiteacher"
        keyPassword = "aiteacher123456"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = false
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

#### `AndroidManifest.xml`

确保已配置应用图标和基本属性：
```xml
<application
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher"
    android:label="AI Teacher"
    ...>
```

---

## 构建 Debug APK

Debug APK 适用于开发和测试，无需签名，构建速度快。

### Windows

在项目根目录打开 PowerShell 或命令提示符，执行：

```powershell
.\gradlew assembleDebug
```

### macOS / Linux

在项目根目录打开终端，执行：

```bash
./gradlew assembleDebug
```

### 输出位置

构建完成后，APK 文件位于：
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 构建 Release APK

Release APK 用于正式发布，需要签名。

### 1. 生成签名密钥（首次构建）

如果还没有签名密钥，需要先生成一个：

#### 使用 keytool（命令行方式）

在项目根目录执行：

**Windows:**
```powershell
keytool -genkeypair -v -keystore aiteacher-release.keystore -alias aiteacher -keyalg RSA -keysize 2048 -validity 10000
```

**macOS / Linux:**
```bash
keytool -genkeypair -v -keystore aiteacher-release.keystore -alias aiteacher -keyalg RSA -keysize 2048 -validity 10000
```

命令执行后会提示您输入：
- Keystore 密码：`aiteacher123456`（与 `build.gradle.kts` 中配置的一致）
- Key 密码：`aiteacher123456`
- 姓名、组织等单位信息（按回车可跳过）

完成后会在项目根目录生成 `aiteacher-release.keystore` 文件。

#### 使用 Android Studio

1. 打开 Android Studio
2. Build → Generate Signed Bundle / APK
3. 选择 APK
4. 点击 "Create new..." 创建新密钥
5. 填写密钥信息
6. 选择签名密钥
7. 选择 Release 构建类型
8. Finish

Sealed

### 2. 构建 Release APK

**Windows:**
```powershell
.\gradlew assembleRelease
```

**macOS / Linux:**
```bash
./gradlew assembleRelease
```

### 输出位置

构建完成后，APK 文件位于：
```
app/build/outputs/apk/release/app-release.apk
```

---

## 安装 APK 到设备

### 方法 1：通过 ADB 安装（推荐）

1. **启用 USB 调试**
   - 进入手机：设置 → 关于手机 → 连续点击 7 次"版本号"开启开发者模式
   - 设置 → 开发者选项 → 启用 "USB 调试"

2. **连接手机到电脑**
   - 使用 USB 数据线连接
   - 手机上点击"允许 USB 调试"

3. **安装 APK**
   
   在命令行执行（Windows 可能需要完整路径）：
   
   **Debug APK:**
   ```bash
   adb install app\build\outputs\apk\debug\app-debug.apk
   ```
   
   **Release APK:**
   ```bash
   adb install app\build\outputs\apk\release\app-release.apk
   ```

### 方法 2：直接传输安装

1. **将 APK 传输到手机**
   - 通过 USB 数据线复制 APK 到手机
   - 或通过微信、邮件等方式发送到手机

2. **在手机上安装**
   - 使用文件管理器打开 APK
   - 点击安装
   - 如提示"禁止安装未知来源应用"，请允许
   - 等待安装完成

### 方法 3：使用 Android Studio

1. 打开项目
2. 连接手机
3. 点击运行按钮（绿色三角形）
4. 选择设备
5. 点击 OK

---

## 常见问题

### 1. 构建失败：找不到签名密钥

**错误信息：**
```
Keystore file 'aiteacher-release.keystore' not found
```

**解决方法：**
- 确保 `aiteacher-release.keystore` 文件存在于项目根目录
- 或使用 `keytool` 重新生成密钥（参考[生成签名密钥](#1-生成签名密钥首次构建)）

### 2. 构建失败：签名密码错误

**错误信息：**
```
Keystore was tampered with, or password was incorrect
```

**解决方法：**
- 检查 `app/build.gradle.kts` 中的密码是否正确
- 或重新生成密钥并更新配置

### 3. 安装失败：INSTALL_FAILED_UPDATE_INCOMPATIBLE

**错误信息：**
```
INSTALL_FAILED_UPDATE_INCOMPATIBLE: Package signatures do not match
```

**解决方法：**
- 卸载手机上已安装的旧版本应用
- 重新安装新版本

### 4. 安装失败：INSTALL_PARSE_FAILED_NO_CERTIFICATES

**错误信息：**
```
INSTALL_PARSE_FAILED_NO_CERTIFICATES
```

**解决方法：**
- 确保使用 Release 构建（已签名）
- 或重新构建 Release APK

### 5. 权限被拒绝：无法访问 Storage

**解决方法：**
- 安装后进入：设置 → 应用 → AI Teacher → 权限
- 允许"存储"权限

### 6. APK 文件太大

**解决方法：**
在 `app/build.gradle.kts` 中启用代码混淆：
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

### 7. 如何在发布前测试 Release APK？

**方法 1：** 在 Android Studio 中使用 Release 构建
```
Run → Edit Configurations → Build Variant → Release
```

**方法 2：** 使用命令行安装 Release APK
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

## 快速参考

### 常用命令

| 操作 | 命令 |
|------|------|
| 清理项目 | `.\gradlew clean` |
| 构建 Debug APK | `.\gradlew assembleDebug` |
| 构建 Release APK | `.\gradlew assembleRelease` |
| 清理并构建 Release | `.\gradlew clean assembleRelease` |
| 检查 ADB 连接 | `adb devices` |
| 卸载应用 | `adb uninstall com.aiteacher` |
| 安装 APK | `adb install -r <APK路径>` |

### 文件位置

| 文件类型 | 路径 |
|---------|------|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| 签名密钥 | `aiteacher-release.keystore` (项目根目录) |
| 日志文件 | `app/build/outputs/logs/` |

---

## 下一步

安装成功后，您可以：

1. 测试应用功能
2. 收集用户反馈
3. 准备发布到应用商店（如 Google Play、应用宝等）
4. 进一步优化性能

---

## 联系与支持

如有问题，请查看：
- 项目 README.md
- Android 官方文档：https://developer.android.com/studio/publish

---

**最后更新：** 2025-01-27  
**适用版本：** AI Teacher v1.0

