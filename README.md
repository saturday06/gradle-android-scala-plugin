gradle-android-scala-plugin
============================

## Getting started

### build.gradle

```Groovy
buildscript {
    repositories {
        mavenCentral()
        maven {
            url "http://saturday06.github.io/gradle-android-scala-plugin/repository/snapshot"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.8.1'
        classpath 'jp.leafytree.android:gradle-android-scala-plugin:1.0-SNAPSHOT'
    }
}

repositories {
    mavenCentral()
}

apply plugin: 'android'
apply plugin: 'android-scala'
apply plugin: 'idea'

android {
    compileSdkVersion 19
    buildToolsVersion '19.0.1'

    defaultConfig {
        minSdkVersion 7
        targetSdkVersion 19
        versionCode 1
        versionName '1.0'
    }

    sourceSets {
        main {
            scala {
                srcDir "src/main/scala" // default: "src/main/scala"
            }
        }

        instrumentTest {
            scala {
                srcDir "src/instrumentTest/scala" // default: "src/instrumentTest/scala"
            }
        }
    }

    buildTypes {
        debug {
            runProguard true // required
            proguardFile file('proguard-android.txt')
        }

        release {
            runProguard true
            proguardFile file('proguard-android.txt')
        }
    }

    scala {
        target "jvm-1.6" // default: "jvm-1.6"
    }
}

dependencies {
    compile 'org.scala-lang:scala-library:2.10.3'
}
```

### proguard-android.txt

```
-dontoptimize
-dontobfuscate
-dontpreverify
-dontwarn scala.**
-keep class !scala.collection.** { *; }
```
