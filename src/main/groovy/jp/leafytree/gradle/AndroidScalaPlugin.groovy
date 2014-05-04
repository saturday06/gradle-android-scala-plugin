/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.leafytree.gradle
import com.google.common.annotations.VisibleForTesting
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.DefaultScalaSourceSet
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.util.ConfigureUtil
import proguard.gradle.ProGuardTask

import javax.inject.Inject
import java.nio.file.FileSystems
import java.nio.file.Files
/**
 * AndroidScalaPlugin adds scala language support to official gradle android plugin.
 */
public class AndroidScalaPlugin implements Plugin<Project> {
    private final FileResolver fileResolver
    @VisibleForTesting
    final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private Project project
    private Object androidPlugin
    private Object androidExtension
    private boolean isLibrary
    private File workDir
    private File scalaProguardFile
    private File testProguardFile
    private final AndroidScalaPluginExtension extension = new AndroidScalaPluginExtension()

    /**
     * Creates a new AndroidScalaPlugin with given file resolver.
     *
     * @param fileResolver the FileResolver
     */
    @Inject
    public AndroidScalaPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    /**
     * Registers the plugin to current project.
     *
     * @param project currnet project
     * @param androidExtension extension of Android Plugin
     */
    void apply(Project project, Object androidExtension) {
        this.project = project
        if (project.plugins.hasPlugin("android-library")) {
            isLibrary = true
            androidPlugin = project.plugins.findPlugin("android-library")
        } else {
            isLibrary = false
            androidPlugin = project.plugins.findPlugin("android")
        }
        this.androidExtension = androidExtension
        this.workDir = new File(project.buildDir, "android-scala")
        this.scalaProguardFile = new File(workDir, "proguard-scala-config.txt")
        this.testProguardFile = new File(workDir, "proguard-test-config.txt")
        updateAndroidExtension()
        updateAndroidSourceSetsExtension()
        project.afterEvaluate {
            androidExtension.testVariants.each { variant ->
                updateTestedVariantProguardTask(variant)
                updateTestVariantProguardTask(variant)
            }
            def allVariants = androidExtension.testVariants + (isLibrary ? androidExtension.libraryVariants : androidExtension.applicationVariants)
            allVariants.each { variant ->
                addAndroidScalaCompileTask(variant)
            }
        }

        // Create proguard configurations
        androidExtension.defaultConfig { proguardFile scalaProguardFile }
        project.tasks.findByName("preBuild").doLast {
            FileUtils.forceMkdir(workDir)
            scalaProguardFile.withWriter { it.write defaultScalaProguardConfig }
            testProguardFile.withWriter { it.write defaultTestProguardConfig }
        }

        // Disable preDexLibraries
        androidExtension.dexOptions.preDexLibraries = false
        project.gradle.taskGraph.whenReady { taskGraph ->
            if (androidExtension.dexOptions.preDexLibraries) {
                throw new GradleException("Currently, android-scala plugin doesn't support enabling dexOptions.preDexLibraries")
            }
        }
    }

    /**
     * Registers the plugin to current project.
     *
     * @param project currnet project
     * @param androidExtension extension of Android Plugin
     */
    public void apply(Project project) {
        if (!project.plugins.findPlugin("android") && !project.plugins.findPlugin("android-library")) {
            throw new GradleException("Please apply 'android' or 'android-library' plugin before applying 'android-scala' plugin")
        }
        apply(project, project.extensions.getByName("android"))
    }

    /**
     * Returns directory for plugin's private working directory for argument
     *
     * @param variant the Variant
     * @return
     */
    File getVariantWorkDir(Object variant) {
        new File([workDir, "variant", variant.name].join(File.separator))
    }

    /**
     * Update tested variant's proguard task to keep classes which test classes depend on
     *
     * @param testVariant the TestVariant
     */
    void updateTestVariantProguardTask(final Object testVariant) {
        def variantWorkDir = getVariantWorkDir(testVariant)
        def dexTask = testVariant.dex
        def proguardTask = project.tasks.create("proguard${testVariant.name.capitalize()}ByAndroidScalaPlugin", ProGuardTask)
        def sourceProguardTask
        if (isLibrary) {
            sourceProguardTask = testVariant.testedVariant.obfuscation
            if (!sourceProguardTask) {
                return
            }
            proguardTask.injars(dexTask.inputFiles)
        } else {
            sourceProguardTask = testVariant.obfuscation
            if (!sourceProguardTask) {
                return
            }
            proguardTask.dependsOn sourceProguardTask
            sourceProguardTask.outJarFiles.each {
                proguardTask.injars(it)
            }
        }
        proguardTask.dependsOn testVariant.javaCompile
        sourceProguardTask.configurationFiles.each { // TODO: Clone all options
            proguardTask.configuration(it)
        }
        proguardTask.configuration(testProguardFile)
        dexTask.dependsOn proguardTask
        def proguardedFile = new File(variantWorkDir, "proguarded-classes.jar")
        proguardTask.doFirst {
            proguardTask.outjars(proguardedFile, filter: "!META-INF/MANIFEST.MF") // TODO: outjars should be delayed ?
        }
        dexTask.inputFiles = [proguardedFile]
        proguardTask.verbose()
        proguardTask.keep("class ${testVariant.packageName}.** { *; }")
        proguardTask.keep("class ${testVariant.testedVariant.packageName}.** { *; }")
        proguardTask.printconfiguration(new File(variantWorkDir, "proguard-all-config.txt"))
        dexTask.libraries.each {
            proguardTask.injars(it)
        }
        (testVariant.javaCompile.classpath.collect { it.canonicalPath } - dexTask.libraries.collect {
            it.canonicalPath
        }).each {
            proguardTask.libraryjars(it)
        }
        dexTask.libraries = []
        proguardTask.doFirst {
            FileUtils.forceMkdir(variantWorkDir)
            androidPlugin.bootClasspath.each {
                proguardTask.libraryjars(it)
            }
        }
    }

    /**
     * Update tested variant's proguard task to keep classes which test classes depend on
     *
     * @param testVariant the TestVariant
     */
    void updateTestedVariantProguardTask(final Object testVariant) {
        if (isLibrary) {
            return
        }
        final def proguardTask = testVariant.testedVariant.obfuscation
        if (!proguardTask) {
            return
        }
        def testJavaCompileTask = testVariant.javaCompile
        final def javaCompileDestinationDir = testJavaCompileTask.destinationDir
        proguardTask.dependsOn(testJavaCompileTask)
        proguardTask.injars(javaCompileDestinationDir)
        proguardTask.keepdirectories(javaCompileDestinationDir.toString())
        testVariant.variantData.variantConfiguration.compileClasspath.each {
            proguardTask.libraryjars(it)
        }
        proguardTask.doLast {
            // TODO: More elegant way
            def testFiles = []
            def baseLength = javaCompileDestinationDir.canonicalPath.length()
            javaCompileDestinationDir.traverse { file ->
                if (!file.isDirectory()) {
                    testFiles.add(file.canonicalPath.substring(baseLength))
                }
            }
            proguardTask.outJarFiles.each { file ->
                def fileSystem = FileSystems.newFileSystem(file.toPath(), null)
                try {
                    testFiles.clone().each { testFile ->
                        if (Files.deleteIfExists(fileSystem.getPath(testFile))) {
                            testFiles.remove(testFile)
                        }
                    }
                } finally {
                    fileSystem.close()
                }
            }
        }
    }

    /**
     * Returns scala version from scala-library in given classpath.
     *
     * @param classpath the classpath contains scala-library
     * @return scala version
     */
    static String scalaVersionFromClasspath(String classpath) {
        def urls = new LinkedList<URL>()
        for (String path : classpath.split(File.pathSeparator)) {
            urls.add(new File(path).toURI().toURL())
        }
        def classLoader = new URLClassLoader(urls.toArray(new URL[0]))
        try {
            def propertiesClass
            try {
                propertiesClass = classLoader.loadClass("scala.util.Properties\$")
            } catch (ClassNotFoundException e) {
                return null
            }
            def versionNumber = propertiesClass.MODULE$.scalaProps["maven.version.number"]
            return versionNumber
        } finally {
            classLoader.close()
        }
    }

    /**
     * Updates AndroidPlugin's root extension to work with AndroidScalaPlugin.
     */
    void updateAndroidExtension() {
        androidExtension.metaClass.getScala = { extension }
        androidExtension.metaClass.scala = { configureClosure ->
            ConfigureUtil.configure(configureClosure, extension)
            androidExtension
        }
    }

    /**
     * Updates AndroidPlugin's sourceSets extension to work with AndroidScalaPlugin.
     */
    void updateAndroidSourceSetsExtension() {
        androidExtension.sourceSets.each { sourceSet ->
            sourceSet.convention.plugins.scala = new DefaultScalaSourceSet(sourceSet.displayName, fileResolver)
            def scala = sourceSet.scala
            def defaultSrcDir = ["src", sourceSet.name, "scala"].join(File.separator)
            def include = "**/*.scala"
            scala.srcDir(defaultSrcDir)
            scala.getFilter().include(include);
            sourceSet.allJava.source(scala)
            sourceSet.allSource.source(scala)
            sourceSet.java.srcDir(defaultSrcDir) // for Android Studio
            sourceDirectorySetMap[sourceSet.name] = scala

            // TODO: more elegant way
            sourceSet.java.getFilter().include(include);
            sourceSet.allSource.getFilter().include(include);
        }
    }

    /**
     * Updates AndroidPlugin's compilation task to support scala.
     *
     * @param task the JavaCompile task
     */
    void addAndroidScalaCompileTask(Object variant) {
        def javaCompileTask = variant.javaCompile
        def scalaVersion = scalaVersionFromClasspath(javaCompileTask.classpath.asPath)
        if (!scalaVersion) {
            return
        }
        project.logger.info("scala-library version=$scalaVersion detected")
        def configurationName = "androidScalaPluginScalaCompilerFor" + javaCompileTask.name
        def configuration = project.configurations.findByName(configurationName)
        if (!configuration) {
            configuration = project.configurations.create(configurationName)
            project.dependencies.add(configurationName, "org.scala-lang:scala-compiler:$scalaVersion")
            project.dependencies.add(configurationName, "com.typesafe.zinc:zinc:0.3.2")
        }
        def variantWorkDir = getVariantWorkDir(variant)
        def destinationDir = new File(variantWorkDir, "scalaCompile") // TODO: More elegant way
        def scalaCompileTask = project.tasks.create("compile${variant.name.capitalize()}Scala", ScalaCompile)
        scalaCompileTask.source = javaCompileTask.source
        scalaCompileTask.destinationDir = destinationDir
        scalaCompileTask.sourceCompatibility = javaCompileTask.sourceCompatibility
        scalaCompileTask.targetCompatibility = javaCompileTask.targetCompatibility
        scalaCompileTask.options.bootClasspath = androidPlugin.bootClasspath.join(File.pathSeparator)
        scalaCompileTask.classpath = javaCompileTask.classpath + project.files(androidPlugin.bootClasspath) // TODO: Remove bootClasspath
        scalaCompileTask.scalaClasspath = configuration.asFileTree
        scalaCompileTask.zincClasspath = configuration.asFileTree
        scalaCompileTask.scalaCompileOptions.incrementalOptions.analysisFile = new File(variantWorkDir, "analysis.txt")
        if (extension.addparams) {
            scalaCompileTask.scalaCompileOptions.additionalParameters = [extension.addparams]
        }
        scalaCompileTask.doFirst {
            FileUtils.forceMkdir(destinationDir)
        }
        javaCompileTask.dependsOn.each {
            scalaCompileTask.dependsOn it
        }
        javaCompileTask.classpath = javaCompileTask.classpath + project.files(destinationDir)
        javaCompileTask.dependsOn scalaCompileTask
        javaCompileTask.doLast {
            project.ant.move(todir: javaCompileTask.destinationDir, preservelastmodified: true) {
                fileset(dir: destinationDir)
            }
        }
    }

    /**
     * Returns the proguard configuration text for test.
     *
     * @return the proguard configuration text for test
     */
    String getDefaultTestProguardConfig() {
        '''
        -dontwarn scala.**
        -ignorewarnings

        # execute shrinking only
        -dontoptimize
        -dontobfuscate
        -dontpreverify
        #-dontshrink

        # standard libraries
        -dontwarn android.**, java.**, javax.microedition.khronos.**, junit.framework.**, scala.**, **.R$*
        -dontnote android.**, java.**, javax.microedition.khronos.**, junit.framework.**, scala.**, **.R$*

        # test libraries
        -dontwarn com.robotium.solo.**, org.mockito.**, junitx.**, com.google.android.apps.common.testing.**
        -dontnote com.robotium.solo.**, org.mockito.**, junitx.**, com.google.android.apps.common.testing.**
        -keep class com.google.dexmaker.mockito.** { *; }

        # for android. see also http://proguard.sourceforge.net/manual/examples.html#androidapplication
        -keep class * extends android.** { *; }
        -keep class * extends junit.** { *; }
        -keep class * implements android.** { *; }
        -keepclasseswithmembers class * {
            public <init>(android.content.Context, android.util.AttributeSet);
        }
        -keepclasseswithmembers class * {
            public <init>(android.content.Context, android.util.AttributeSet, int);
        }
        -keepclassmembers class * {
            @android.webkit.JavascriptInterface <methods>;
        }
        -keep public class com.google.vending.licensing.ILicensingService
        -keep public class com.android.vending.licensing.ILicensingService
        -dontnote com.google.vending.licensing.ILicensingService
        -dontnote com.android.vending.licensing.ILicensingService
        -keepclassmembers enum * {
            public static **[] values();
            public static ** valueOf(java.lang.String);
        }

        # miscellaneous. see also http://proguard.sourceforge.net/index.html#manual
        -keepattributes *Annotation*
        -keepclasseswithmembernames class * { native <methods>; }
        '''
    }

    /**
     * Returns the proguard configuration text for scala.
     *
     * @return the proguard configuration text for scala
     */
    String getDefaultScalaProguardConfig() {
        '''
        # for scala. see also http://proguard.sourceforge.net/manual/examples.html#scala
        -keep class scala.collection.SeqLike { public protected *; } # https://issues.scala-lang.org/browse/SI-5397
        -keep class scala.reflect.ScalaSignature { *; }
        -keep class scala.reflect.ScalaLongSignature { *; }
        -keep class scala.Predef$** { *; }
        -keepclassmembers class * { ** MODULE$; }
        -keep class * implements org.xml.sax.EntityResolver
        -keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
            long eventCount;
            int  workerCounts;
            int  runControl;
            scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
            scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
        }
        -keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
            int base;
            int sp;
            int runState;
        }
        -keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
            int status;
        }
        -keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
            scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
            scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
            scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
        }
        '''
    }
}
