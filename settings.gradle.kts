@file:Suppress("UnstableApiUsage")
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    val agpVersion: String by settings
    val kotlinVersion: String by settings
    plugins {
        id("com.android.application") version (agpVersion) apply (false)
        id("com.android.library") version (agpVersion) apply (false)
        kotlin("android") version (kotlinVersion) apply (false)
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver") version "0.7.0"
}
toolchainManagement{
    jvm {
        javaRepositories {
            repository("foojay") {
                resolverClass.set(org.gradle.toolchains.foojay.FoojayToolchainResolver::class.java)
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url=uri("https://jitpack.io") }
    }
}

rootProject.name = "ZhihuMatisse"
include(":sample",":matisse")
