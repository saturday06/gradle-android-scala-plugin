# gradle-android-scala-plugin [![Build Status](https://travis-ci.org/saturday06/gradle-android-scala-plugin.png?branch=master)](https://travis-ci.org/saturday06/gradle-android-scala-plugin)

gradle-android-scala-plugin adds scala language support to official gradle android plugin.
See also sample projects at https://github.com/saturday06/gradle-android-scala-plugin/tree/master/sample

## Supported versions

| Scala  | Gradle | Android Plugin | compileSdkVersion | buildToolsVersion | Incremental compilation |
| ------ | ------ | -------------- | ----------------- | ----------------- | ----------------------- |
| 2.10.4 | 2.1    | 0.14.0         | android-21        | 21.1              | enabled                 |
| 2.11.4 | 2.1    | 0.14.0         | android-21        | 21.1              | disabled                |

## Installation

### 1. Add buildscript's dependency

`build.gradle`
```groovy
buildscript {
    dependencies {
        classpath "com.android.tools.build:gradle:0.14.0"
        classpath "jp.leafytree.gradle:gradle-android-scala-plugin:1.1"
    }
}
```

### 2. Apply plugin

`build.gradle`
```groovy
apply plugin: "com.android.application"
apply plugin: "jp.leafytree.android-scala"
```

### 3. Add scala-library dependency

The plugin decides scala language version using scala-library's version.

`build.gradle`
```groovy
dependencies {
    compile "org.scala-lang:scala-library:2.11.4"
}
```

### 4. Put scala source files

Default locations are src/main/scala, src/androidTest/scala.
You can customize those directories similar to java.

`build.gradle`
```groovy
android {
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
}
```

### 5. Setup MultiDexApplication

`build.gradle`
```groovy
android {
    defaultConfig {
        multiDexEnabled true
    }
}
```

`AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="...">
    <application android:name="android.support.multidex.MultiDexApplication">
    </application>
</manifest>
```

See also: http://developer.android.com/tools/building/multidex.html

## Complete example of build.gradle

`build.gradle`
```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath "com.android.tools.build:gradle:0.14.0"
        classpath "jp.leafytree.gradle:gradle-android-scala-plugin:1.1"
    }
}

repositories {
    jcenter()
}

apply plugin: "com.android.application"
apply plugin: "jp.leafytree.android-scala"

android {
    compileSdkVersion "android-21"
    buildToolsVersion "21.1"

    defaultConfig {
        minSdkVersion 8
        targetSdkVersion 21
        versionCode 1
        versionName "1.0"
        multiDexEnabled true
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

    scala {
        addparams "-deprecation"            // default: null
        additionalParameters "-deprecation" // alias of addparams
    }
}

dependencies {
    compile "org.scala-lang:scala-library:2.11.4"
}
```

### Changelog
- 1.2 Incremental compilation support in scala 2.10
- 1.1 MultiDexApplication support
- 1.0 First release
