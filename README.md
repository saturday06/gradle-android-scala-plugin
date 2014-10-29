# gradle-android-scala-plugin [![Build Status](https://travis-ci.org/saturday06/gradle-android-scala-plugin.png?branch=master)](https://travis-ci.org/saturday06/gradle-android-scala-plugin)

gradle-android-scala-plugin adds scala language support to official gradle android plugin.

See also:
- https://code.google.com/p/android/issues/detail?id=56231
- https://code.google.com/p/android/issues/detail?id=56232
- https://code.google.com/p/android/issues/detail?id=63936

## Supported versions

| Scala  | Gradle | Android Plugin | compileSdkVersion | buildToolsVersion |
| ------ | ------ | -------------- | ----------------- | ----------------- |
| 2.10.4 | 2.1    | 0.13.3         | android-21        | 21.0.2            |
| 2.11.2 | 2.1    | 0.13.3         | android-21        | 21.0.2            |

## Configuration example

### build.gradle

```Groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath "com.android.tools.build:gradle:0.13.3"
        classpath "jp.leafytree.gradle:gradle-android-scala-plugin:1.1"
    }
}

repositories {
    jcenter()
}

apply plugin: "com.android.application"
apply plugin: "android-scala"

android {
    compileSdkVersion "android-21"
    buildToolsVersion "21.0.2"

    defaultConfig {
        minSdkVersion 8
        targetSdkVersion 20
        versionCode 1
        versionName "1.0"
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
}

dependencies {
    compile "com.google.android:multidex:0.1"
    compile "org.scala-lang:scala-library:2.11.2"
}

afterEvaluate {
    tasks.matching {
        it.name.startsWith('dex')
    }.each { dx ->
        if (dx.additionalParameters == null) {
            dx.additionalParameters = []
        }
        dx.additionalParameters += '--multi-dex'
        dx.additionalParameters += "--main-dex-list=$rootDir/main-dex-list.txt".toString()
    }
}
```

### main-dex-list.txt

```
android/support/multidex/BuildConfig.class
android/support/multidex/MultiDex$V14.class
android/support/multidex/MultiDex$V19.class
android/support/multidex/MultiDex$V4.class
android/support/multidex/MultiDex.class
android/support/multidex/MultiDexApplication.class
android/support/multidex/MultiDexExtractor$1.class
android/support/multidex/MultiDexExtractor.class
android/support/multidex/ZipUtil$CentralDirectory.class
android/support/multidex/ZipUtil.class
```

### src/androidTest/java/com/android/test/runner/MultiDexTestRunner.java
https://github.com/casidiablo/multidex/blob/publishing/instrumentation/src/com/android/test/runner/MultiDexTestRunner.java
```
package com.android.test.runner;

import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.test.InstrumentationTestRunner;

public class MultiDexTestRunner extends InstrumentationTestRunner {
    @Override
    public void onCreate(Bundle arguments) {
        MultiDex.install(getTargetContext());
        super.onCreate(arguments);
    }
}
```

### Changelog
- 1.1 MultiDexApplication support
- 1.0 First release
