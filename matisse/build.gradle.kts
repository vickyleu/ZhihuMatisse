/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
}

group = "com.github.vickyleu"
version = "1.0.1"


android {
    compileSdk = 34
    namespace = "com.zhihu.matisse"
    defaultConfig {
        minSdk = 21
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    dataBinding {
        enable = true
    }
    lint {
        abortOnError = false
        disable.add("MissingTranslation")
        disable.add("ExtraTranslation")
        disable.add("InvalidPlurals")
        disable.add("MissingDefaultResource")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.github.DylanCaiCoding:ActivityResultLauncher:1.1.2")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0-alpha13")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
//    implementation("it.sephiroth.android.library.imagezoom:library:1.0.4")
    implementation("it.sephiroth.android.library.imagezoom:imagezoom:2.3.0")
    implementation("io.fotoapparat:fotoapparat:2.7.0")
    compileOnly("com.github.bumptech.glide:glide:4.16.0")
}

afterEvaluate {
    publishing {
        publications {
            // Creates a Maven publication called "release".
            register("release", MavenPublication::class) {
                // Library Package Name (Example : "com.frogobox.androidfirstlib")
                // NOTE : Different GroupId For Each Library / Module, So That Each Library Is Not Overwritten
                groupId = "com.github.vickyleu"
                // Library Name / Module Name (Example : "androidfirstlib")
                // NOTE : Different ArtifactId For Each Library / Module, So That Each Library Is Not Overwritten
                artifactId = "matisse"
                // Version Library Name (Example : "1.0.0")
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}
