import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.70"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation("org.quartz-scheduler:quartz-jobs:2.3.2")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("org.hsqldb:hsqldb:2.5.0")
    implementation("org.liquibase:liquibase-core:3.8.7")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}


configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}