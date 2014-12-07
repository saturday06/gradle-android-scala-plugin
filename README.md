# gradle-android-scala-plugin [![Build Status](https://travis-ci.org/saturday06/gradle-android-scala-plugin.png?branch=master)](https://travis-ci.org/saturday06/gradle-android-scala-plugin)

gradle-android-scala-plugin adds scala language support to official gradle android plugin.
See also sample projects at https://github.com/saturday06/gradle-android-scala-plugin/tree/master/sample

- [Supported versions](#supported-versions)
- [Installation](#installation)
  - [1. Add buildscript's dependency](#1-add-buildscripts-dependency)
  - [2. Apply plugin](#2-apply-plugin)
  - [3. Add scala-library dependency](#3-add-scala-library-dependency)
  - [4. Put scala source files](#4-put-scala-source-files)
  - [5. Setup MultiDexApplication](#5-setup-multidexapplication)
  - [6. Setup application class if you use customized one](#6-setup-application-class-if-you-use-customized-one)
- [Complete example of build.gradle](#complete-example-of-buildgradle)
- [Changelog](#changelog)

## Supported versions

| Scala  | Gradle | Android Plugin | compileSdkVersion | buildToolsVersion |
| ------ | ------ | -------------- | ----------------- | ----------------- |
| 2.11.4 | 2.2.1  | 1.0.0-rc4      | android-21        | 21.1.1            |
| 2.10.4 | 2.2.1  | 1.0.0-rc4      | android-21        | 21.1.1            |
| 2.11.4 | 1.12   | 0.12.2         | android-21        | 21.1.1            |
| 2.10.4 | 1.12   | 0.12.2         | android-21        | 21.1.1            |
| 2.11.4 | 1.12   | 0.12.2         | android-19        | 19.1.0            |
| 2.10.4 | 1.12   | 0.12.2         | android-19        | 19.1.0            |

## Installation

### 1. Add buildscript's dependency

`build.gradle`
```groovy
buildscript {
    dependencies {
        classpath "com.android.tools.build:gradle:1.0.0-rc4"
        classpath "jp.leafytree.gradle:gradle-android-scala-plugin:1.3.1"
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

To avoid https://code.google.com/p/android/issues/detail?id=20814 we should setup MultiDexApplication.
See also https://github.com/casidiablo/multidex . There is `multiDexEnabled` option but it can't be
used because it causes `DexException: Too many classes in --main-dex-list, main dex capacity exceeded` .

**NOTE: Use of multidex for creating a test APK is not currently supported.**
[Described in google's documentation](https://developer.android.com/tools/building/multidex.html#testing)

`build.gradle`
```groovy
repositories {
    jcenter()
}

android {
    dexOptions {
        preDexLibraries false
        javaMaxHeapSize "2g"
    }
}

dependencies {
    compile "com.android.support:multidex:1.0.0"
    compile "org.scala-lang:scala-library:2.11.4"
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
com/android/test/runner/MultiDexTestRunner.class
```

Change application class.

`AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="jp.leafytree.sample">
    <application android:name="android.support.multidex.MultiDexApplication">
</manifest>
```

If you use customized application class, please read [next section](#6-setup-application-class-if-you-use-customized-one).

To test MultiDexApplication, custom instrumentation test runner should be used.
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

### 6. Setup application class if you use customized one

Since application class is executed **before** multidex configuration,
Writing custom application class has stll many pitfalls.

The application class must extend MultiDexApplication or override
`Application#attachBaseContext` like following.

`MyCustomApplication.scala`
```scala
package my.custom.application

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import my.custom.application.main.{ClassNotNeededToBeListed, ClassNeededToBeListed}

object MyCustomApplication {
  var globalVariable: Int = _
}

class MyCustomApplication extends Application {
  override protected def attachBaseContext(base: Context) = {
    super.attachBaseContext(base)
    MultiDex.install(this)
  }

  var variable: ClassNeededToBeListed = _
}
```

Add your customized application class to `main-dex-list.txt`.

`main-dex-list.txt`
```text
android/support/multidex/BuildConfig.class
android/support/multidex/MultiDex$V14.class
android/support/multidex/MultiDex$V19.class
...
my/custom/application/MyCustomApplication.class
my/custom/application/MyCustomApplication$$anon$1.class
my/custom/application/main/ClassNeededToBeListed.class
```

**You need to remember:**

NOTE: The following cautions must be taken only on your android Application class, you don't need to apply this cautions in all classes of your app

- The static fields in your **application class** will be loaded before the `MultiDex#install`be called! So the suggestion is to avoid static fields with types that can be placed out of main classes.dex file.
- The methods of your **application class** may not have access to other classes that are loaded after your application class. As workaround for this, you can create another class (any class, in the example above, I use Runnable) and execute the method content inside it. Example:

```scala
  override def onCreate = {
    super.onCreate

    val context = this
    new Runnable {
      override def run = {
        variable = new ClassNeededToBeListed(context, new ClassNotNeededToBeListed)
        MyCustomApplication.globalVariable = 100
      }
    }.run
  }
```

This section is copyed from
[README.md for multidex project](https://github.com/casidiablo/multidex/blob/5a6e7f6f7fb43ba41465bb99cc1de1bd9c1a3a3a/README.md#cautions)

## Complete example of build.gradle

`build.gradle`
```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath "com.android.tools.build:gradle:1.0.0-rc4"
        classpath "jp.leafytree.gradle:gradle-android-scala-plugin:1.3.1"
    }
}

repositories {
    jcenter()
}

apply plugin: "com.android.application"
apply plugin: "jp.leafytree.android-scala"

android {
    compileSdkVersion "android-21"
    buildToolsVersion "21.1.1"

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
        javaMaxHeapSize "2g"
    }
}

dependencies {
    compile "com.android.support:multidex:1.0.0"
    compile "org.scala-lang:scala-library:2.11.4"
}

tasks.withType(ScalaCompile) {
    scalaCompileOptions.deprecation = false
    scalaCompileOptions.additionalParameters = ["-feature"]
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

## Changelog
- 1.3.1 Support android plugin 12.2
- 1.3 Incremental compilation support in scala 2.11
- 1.2.1 Fix binary compatibility with JDK6
- 1.2 Incremental compilation support in scala 2.10 / Flavors support
- 1.1 MultiDexApplication support
- 1.0 First release
