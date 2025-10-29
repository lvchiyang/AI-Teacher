pluginManagement {
    repositories {
        // 插件镜像放最前面
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven("https://mirrors.cloud.tencent.com/gradle/android-plugin")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://jitpack.io") }
        maven("https://mirrors.cloud.tencent.com/gradle/android")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "AITeacher"
include(":app")