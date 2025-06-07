plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    kotlin("plugin.serialization") version "2.1.21"
}

group = "com.milkcocoa.info"
version = "1.0.0"
application {
    mainClass.set("com.milkcocoa.info.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.latteCore)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.resources)
    implementation(libs.kotlinx.serialization.properties)
    implementation(libs.ktor.server.negotiation)
    implementation(libs.ktor.server.ratelimit)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.serialization.json)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

kotlin{
    jvmToolchain(17)
}