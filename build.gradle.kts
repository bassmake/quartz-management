import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    java
    kotlin("jvm") version "1.3.71"
    id("com.github.ben-manes.versions") version "0.28.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.microutils:kotlin-logging:1.7.9")
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation("org.quartz-scheduler:quartz-jobs:2.3.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.hsqldb:hsqldb:2.5.0")
    implementation("org.liquibase:liquibase-core:3.8.8")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.1")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<DependencyUpdatesTask> {
    fun isNonStable(version: String): Boolean {
        return listOf("alpha").any {  version.toLowerCase().contains(it)  }
    }
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}