plugins {
    kotlin("multiplatform") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
    kotlin("plugin.compose") version "2.2.10"
    id("org.jetbrains.compose") version "1.7.1"
    id("com.android.application") version "8.13.0"
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
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

compose.desktop {
    application {
        mainClass = "com.aiteacher.MainKt"
    }
}

val exposedVersion = "0.50.1"

kotlin {
    androidTarget()
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                
                // 共享业务逻辑依赖
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("io.github.oshai:kotlin-logging:7.0.13")

                implementation("io.modelcontextprotocol:kotlin-sdk:0.7.3")
                
            }
        }
        
        
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.8.0")
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                
                // Navigation
                implementation("androidx.navigation:navigation-compose:2.7.5")
                
                // AppCompat
                implementation("androidx.appcompat:appcompat:1.6.1")
                
                // Material Design
                implementation("androidx.compose.material3:material3:1.3.2")

                // Android特定数据库依赖
                implementation("androidx.room:room-runtime:2.6.0")
                implementation("androidx.room:room-ktx:2.6.0")

                // 阿里云DashScope SDK - 使用更兼容的版本
                implementation("com.alibaba:dashscope-sdk-java:2.20.0")

                
                // Ktor 依赖 (Android只需要客户端)
                implementation("io.ktor:ktor-client-cio:2.3.12")        // 客户端
                // 服务器端依赖在Android中不需要
                // implementation("io.ktor:ktor-server-netty:3.3.0")       // 服务器
                // implementation("io.ktor:ktor-server-sse:3.3.0")        // SSE 支持
                // implementation("io.ktor:ktor-server-websockets:3.3.0") // WebSocket 支持
                
                // 日志
                implementation("org.slf4j:slf4j-simple:2.0.17")

                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion") // 支持 java.time 类型
            
                // Android使用SQLite，不需要MySQL连接器
                implementation("org.xerial:sqlite-jdbc:3.46.0.0")
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.uiTooling)
                
                // Desktop特定依赖
                implementation("org.xerial:sqlite-jdbc:3.44.1.0")
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                
                // 阿里云DashScope SDK - 桌面端支持
                implementation("com.alibaba:dashscope-sdk-java:2.20.0")
                
                // Ktor 依赖 (桌面端需要客户端)
                implementation("io.ktor:ktor-client-cio:2.3.12")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }
    }
}
