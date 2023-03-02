plugins {
    id("java-library")
}

repositories {
    mavenLocal()
}

dependencies {
    api(project(":api"))
    compileOnly("it.unimi.dsi:fastutil:8.5.6")
}

labyModProcessor {
    referenceType = net.labymod.gradle.core.processor.ReferenceType.DEFAULT
}

tasks.compileJava {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}