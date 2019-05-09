import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.gavin"
version = "0.0.1"

plugins {
    `build-scan`
    `java-library`

    id("org.springframework.boot") version "2.1.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"

    kotlin("jvm") version "1.2.71"
    kotlin("plugin.spring") version "1.2.71"
}

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")

    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("com.google.guava:guava:23.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.1")
    implementation("org.jsoup:jsoup:1.11.3")
    implementation("org.apache.poi:poi:4.0.1")
    implementation("org.tmatesoft.svnkit:svnkit:1.9.3")

    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}

repositories {
    jcenter()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
