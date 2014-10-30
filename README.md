# gradle-android-scala-plugin [![Build Status](https://travis-ci.org/saturday06/gradle-android-scala-plugin.png?branch=master)](https://travis-ci.org/saturday06/gradle-android-scala-plugin)

gradle-android-scala-plugin adds scala language support to official gradle android plugin.
See also sample projects at https://github.com/saturday06/gradle-android-scala-plugin/tree/master/sample

## Supported versions

| Scala  | Gradle | Android Plugin | compileSdkVersion | buildToolsVersion |
| ------ | ------ | -------------- | ----------------- | ----------------- |
| 2.10.4 | 2.1    | 0.13.3         | android-21        | 21.0.2            |
| 2.11.2 | 2.1    | 0.13.3         | android-21        | 21.0.2            |

## Installation

### 1. Add buildscript's dependency

`build.gradle`
```groovy
buildscript {
    dependencies {
        classpath "com.android.tools.build:gradle:0.13.3"
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
    compile "org.scala-lang:scala-library:2.11.2"
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

To avoid https://code.google.com/p/android/issues/detail?id=20814 we should setup MultiDexApplication.
See also https://github.com/casidiablo/multidex

`build.gradle`
```groovy
repositories {
    jcenter()
}

android {
    dexOptions {
        preDexLibraries false
    }
}

dependencies {
    compile "com.google.android:multidex:0.1"
    compile "org.scala-lang:scala-library:2.11.2"
}

afterEvaluate {
    tasks.matching {
        it.name.startsWith("dex")
    }.each { dx ->
        if (dx.additionalParameters == null) {
            dx.additionalParameters = []
        }
        dx.additionalParameters += "--multi-dex"
        dx.additionalParameters += "--main-dex-list=$rootDir/main-dex-list.txt".toString()
    }
}
```

Add main dex configuration.

`main-dex-list.txt`
```text
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

Change application class.

`AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="jp.leafytree.sample">
    <application android:name="android.support.multidex.MultiDexApplication">
</manifest>
```

For androidTest, custom instrumentation test runner should be used.
See also https://github.com/casidiablo/multidex/blob/publishing/instrumentation/src/com/android/test/runner/MultiDexTestRunner.java

`build.gradle`
```groovy
android {
  defaultConfig {
    testInstrumentationRunner "com.android.test.runner.MultiDexTestRunner"
  }
}
```

`src/androidTest/java/com/android/test/runner/MultiDexTestRunner.java`
```java
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

## Complete example of build.gradle

`build.gradle`
```groovy
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
apply plugin: "jp.leafytree.android-scala"

android {
    compileSdkVersion "android-21"
    buildToolsVersion "21.0.2"

    defaultConfig {
        minSdkVersion 8
        targetSdkVersion 21
        testInstrumentationRunner "com.android.test.runner.MultiDexTestRunner"
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

    dexOptions {
        preDexLibraries false
    }

    scala {
        addparams "-deprecation"            // default: null
        additionalParameters "-deprecation" // alias of addparams
    }
}

dependencies {
    compile "com.google.android:multidex:0.1"
    compile "org.scala-lang:scala-library:2.11.2"
}

afterEvaluate {
    tasks.matching {
        it.name.startsWith("dex")
    }.each { dx ->
        if (dx.additionalParameters == null) {
            dx.additionalParameters = []
        }
        dx.additionalParameters += "--multi-dex"
        dx.additionalParameters += "--main-dex-list=$rootDir/main-dex-list.txt".toString()
    }
}
```

### Changelog
- 1.1 MultiDexApplication support
- 1.0 First release
