import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.gavin"
version = "0.0.1"

val ktor_version = "1.1.4"


plugins {
    application
    `build-scan`
    `java-library`

    kotlin("jvm") version "1.3.31"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")

    implementation("org.apache.httpcomponents:httpclient:4.5.1")
    implementation("org.jsoup:jsoup:1.11.3")
    implementation("org.tmatesoft.svnkit:svnkit:1.9.3")

    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
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