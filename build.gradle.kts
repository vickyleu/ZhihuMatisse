//需要判断是否是jitpack的构建，如果是jitpack的构建，需要将build目录设置到项目根目录下
 if (System.getenv("JITPACK") == null) {
     rootProject.layout.buildDirectory.set(file("${rootProject.rootDir.parentFile.parentFile.absolutePath}/buildOut"))
 }
plugins {
    idea
    id("com.android.application") apply (false)
    id("com.android.library") apply (false)
    kotlin("android") apply (false)
}

allprojects {
    val kotlinVersion: String by properties
    this.ext.set("kotlin_version", kotlinVersion)
    if (System.getenv("JITPACK") == null) {
        this.layout.buildDirectory.set(file("${rootProject.layout.buildDirectory.get().asFile.absolutePath}/${project.name}"))
    }
}