rootProject.layout.buildDirectory.set(file("${rootProject.rootDir.parentFile.parentFile.absolutePath}/buildOut"))

plugins {
    idea
    id("com.android.application") apply (false)
    id("com.android.library") apply (false)
    kotlin("android") apply (false)
}

allprojects {
    val kotlinVersion: String by properties
    this.ext.set("kotlin_version", kotlinVersion)
    this.layout.buildDirectory.set(file("${rootProject.layout.buildDirectory.get().asFile.absolutePath}/${project.name}"))
}