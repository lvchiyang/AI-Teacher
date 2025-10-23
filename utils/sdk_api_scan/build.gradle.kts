plugins {
    kotlin("jvm") version "2.2.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // 目标依赖
    implementation("io.modelcontextprotocol:kotlin-sdk:0.7.3")
    
    // 反射和扫描工具
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.classgraph:classgraph:4.8.174")
}

application {
    mainClass.set("ApiDumpKt")
}
