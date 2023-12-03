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

plugins{
    id("com.android.library")
    id("checkstyle")
    kotlin("android")
}


android {
    compileSdk = 34
    namespace = "com.zhihu.matisse"
    defaultConfig {
        minSdk = 21
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
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
//    implementation("it.sephiroth.android.library.imagezoom:library:1.0.4")
    implementation("it.sephiroth.android.library.imagezoom:imagezoom:2.3.0")
    implementation("io.fotoapparat:fotoapparat:2.7.0")
    compileOnly("com.github.bumptech.glide:glide:4.16.0")
}

//tasks.withType(JavaCompile) {
//    options.encoding = "UTF-8"
//}


//task javadoc(type: Javadoc) {
//    options.encoding = "utf-8"
//}
//
//checkstyle {
//    toolVersion = '7.6.1'
//}
//
//tasks.withType(Javadoc) {
//    options.addStringOption('Xdoclint:none', '-quiet')
//    options.addStringOption('encoding', 'UTF-8')
//}
//
//task checkstyle(type:Checkstyle) {
//    description 'Runs Checkstyle inspection against matisse sourcesets.'
//    group = 'Code Quality'
//    configFile rootProject.file('checkstyle.xml')
//    ignoreFailures = false
//    showViolations true
//    classpath = files()
//    source 'src/main/java'
//}
