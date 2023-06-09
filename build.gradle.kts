plugins {
    kotlin("jvm") version "1.8.20"
    kotlin("kapt") version "1.8.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kapt {
    useBuildCache = false
}


dependencies {
    implementation("com.google.dagger:dagger:2.45")
    kapt("com.google.dagger:dagger-compiler:2.45")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}


application {
    mainClass.set("MainKt")
}