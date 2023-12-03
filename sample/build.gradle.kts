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
    id("com.android.application")
}
android {
    compileSdk = 34
    namespace = "com.zhihu.matisse.sample"
    defaultConfig {
        applicationId =  "com.zhihu.matisse.sample"
        minSdk = 21
        versionCode = 1
        versionName = "1.0"
    }
    lint {
        abortOnError = false
    }
    compileOptions {
        sourceCompatibility =  JavaVersion.VERSION_17
        targetCompatibility =  JavaVersion.VERSION_17
    }
}


dependencies {
    implementation(project(":matisse"))

//    implementation 'com.zhihu.android:matisse:0.5.2'
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.tbruyelle:rxpermissions:0.12")
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")
    implementation("com.github.bumptech.glide:glide:4.15.1")
}
