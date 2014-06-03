**Since it's still unstable and under development, please don't use it in production app.**

# gradle-android-scala-plugin [![Build Status](https://travis-ci.org/saturday06/gradle-android-scala-plugin.png?branch=master)](https://travis-ci.org/saturday06/gradle-android-scala-plugin)

gradle-android-scala-plugin adds scala language support to official gradle android plugin.

See also:
- https://code.google.com/p/android/issues/detail?id=56231
- https://code.google.com/p/android/issues/detail?id=56232
- https://code.google.com/p/android/issues/detail?id=63936

## Supported versions

| Scala  | Gradle | Android Plugin | compileSdkVersion | buildToolsVersion |
| ------ | ------ | -------------- | ----------------- | ----------------- |
| 2.10.4 | 1.12   | 0.10.4         | 19                | 19.1              |
| 2.11.1 | 1.12   | 0.10.4         | 19                | 19.1              |

## Configuration example

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
        classpath 'com.android.tools.build:gradle:0.10.4'
        classpath 'jp.leafytree.gradle:gradle-android-scala-plugin:1.0-SNAPSHOT'
    }
}

repositories {
    mavenCentral()
}

apply plugin: 'android'
apply plugin: 'android-scala'

android {
    compileSdkVersion 19
    buildToolsVersion '19.1'

    defaultConfig {
        minSdkVersion 4
        targetSdkVersion 19
        versionCode 1
        versionName '1.0'
    }

    sourceSets {
        main {
            scala {
                srcDir "path/to/main/scala" // default: "src/main/scala"
            }
        }

        androidTest {
            scala {
                srcDir "path/to/androidTest/scala" // default: "src/androidTest/scala"
            }
        }
    }

    buildTypes {
        debug {
            runProguard true // required
            proguardFile file('proguard-rules.txt')
        }

        release {
            runProguard true
            proguardFile file('proguard-rules.txt')
        }
    }
}

dependencies {
    compile 'org.scala-lang:scala-library:2.11.1'
}
```

### proguard-rules.txt

```
-dontoptimize
-dontobfuscate
-dontpreverify
-dontwarn scala.**
-keep class !scala*.** { *; }
-ignorewarnings
```
