plugins {
    id("com.android.application") version "8.13.0"
    id("org.jetbrains.kotlin.android") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
}

android {
    namespace = "com.aiteacher"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.aiteacher"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    
    // 签名配置
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
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    // 配置 Lint
    lint {
        // 不因为 lint 错误而终止构建
        abortOnError = false
        // 忽略本地属性文件的转义警告（这是自动生成的文件）
        disable.add("PropertyEscape")
    }
}

dependencies {
    // Android Compose
    // implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Navigation
    // implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // AppCompat
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Material Design
    // implementation("androidx.compose.material3:material3:1.12.0")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.ui:ui:1.5.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.8")
    implementation("androidx.compose.foundation:foundation:1.5.8")

    // Room 数据库
    implementation("androidx.room:room-runtime:2.8.0")
    implementation("androidx.room:room-ktx:2.8.0")
    ksp("androidx.room:room-compiler:2.8.0")

    // 阿里云DashScope SDK
    implementation("com.alibaba:dashscope-sdk-java:2.20.0")

    // Ktor 客户端
    implementation("io.ktor:ktor-client-cio:2.3.12")
    
    // 日志
    implementation("org.slf4j:slf4j-simple:2.0.17")
    
    // Koin 依赖注入
    implementation("io.insert-koin:koin-android:3.5.6")
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    // Service
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // 日志
    implementation("io.github.oshai:kotlin-logging:7.0.13")

    // Gson for JSON serialization/deserialization
    implementation("com.google.code.gson:gson:2.13.2")
    
}

// 修复 Android Gradle Plugin 8.x 中缺失的 testClasses 任务
// 这个任务在旧版本的 Gradle 中存在，但在 AGP 8.x 中已被移除
afterEvaluate {
    tasks.register("testClasses") {
        group = "verification"
        description = "编译测试类（兼容性任务）"
        
        // 依赖于 Android 项目的测试编译任务
        val testTasks = listOfNotNull(
            tasks.findByName("compileDebugUnitTestKotlin"),
            tasks.findByName("compileReleaseUnitTestKotlin"),
            tasks.findByName("compileDebugUnitTestJavaWithJavac"),
            tasks.findByName("compileReleaseUnitTestJavaWithJavac")
        )
        
        if (testTasks.isNotEmpty()) {
            dependsOn(testTasks)
        } else {
            // 如果没有找到测试任务，至少标记为完成（某些项目可能没有测试）
            doLast {
                println("警告: 未找到测试编译任务，跳过 testClasses")
            }
        }
    }
}