import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.3.40"
val ktorVersion = "1.1.4"

group = "com.gavin"
version = "0.0.1"

plugins {
    application
    `build-scan`
    `java-library`

    kotlin("jvm") version "1.3.40"
}

dependencies {
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("stdlib-js", kotlinVersion))

    implementation("io.ktor", "ktor-server-netty", ktorVersion)
    implementation("io.ktor", "ktor-jackson", ktorVersion)

    implementation("org.apache.httpcomponents", "httpclient", "4.5.1")
    implementation("org.jsoup", "jsoup", "1.11.3")
    implementation("org.tmatesoft.svnkit", "svnkit", "1.9.3")
    implementation("io.github.microutils", "kotlin-logging", "1.6.24")
    implementation("org.slf4j", "slf4j-simple", "1.7.26")

    // Use JUnit test framework
    testImplementation(kotlin("test", kotlinVersion))
    testImplementation(kotlin("test-junit5", kotlinVersion))
}

repositories {
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

