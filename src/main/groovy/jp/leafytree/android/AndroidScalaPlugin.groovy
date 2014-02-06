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
package jp.leafytree.android

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.ConfigureUtil

import javax.inject.Inject

public class AndroidScalaPlugin implements Plugin<Project> {
    private static final String DEX2JAR_VERSION = "0.0.9.15"
    private final FileResolver fileResolver
    private final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private final AndroidScalaPluginExtension extension = new AndroidScalaPluginExtension()
    private Boolean library

    @Inject
    public AndroidScalaPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    public void apply(Project project) {
        def androidExtension = project.extensions.getByName("android")
        library = !!project.plugins.findPlugin("android-library")
        updateAndroidExtension(project, androidExtension)
        updateAndroidSourceSetsExtension(project, androidExtension)
        project.gradle.taskGraph.whenReady { taskGraph ->
            addScalaDependencies(project)
            taskGraph.allTasks.each { Task task ->
                updateAndroidJavaCompileTask(project, androidExtension, task)
            }
            taskGraph.beforeTask { Task task ->
                proguardBeforeDexDebugTestTask(project, task)
            }
        }
    }

    void addScalaDependencies(Project project) {
        def scalaLibraryDependency = project.configurations.compile.allDependencies.find {
            it.group == 'org.scala-lang' && it.name == "scala-library"
        }
        if (!scalaLibraryDependency) {
            throw new GradleException("Dependency `compile \"org.scala-lang:scala-library:\$version\"' is not defined")
        }
        def version = scalaLibraryDependency.version
        project.configurations { scalaCompileProvided }
        project.dependencies.add("scalaCompileProvided", "org.scala-lang:scala-compiler:$version")

        if (library) {
            project.repositories.maven { url "http://repository-dex2jar.forge.cloudbees.com/release/" }
            project.configurations { proguardForLibraryTest }
            project.dependencies.add("proguardForLibraryTest", "net.sf.proguard:proguard-anttask:4.11")
            project.dependencies.add("proguardForLibraryTest", "com.googlecode.dex2jar:dex-tools:$DEX2JAR_VERSION@zip")
        }
    }

    void updateAndroidExtension(Project project, Object androidExtension) {
        androidExtension.metaClass.getScala = { extension }
        androidExtension.metaClass.scala = { configureClosure ->
            ConfigureUtil.configure(configureClosure, extension)
            androidExtension
        }
    }

    void updateAndroidSourceSetsExtension(Project project, Object androidExtension) {
        ["main", "instrumentTest"].each { sourceSetName ->
            def defaultSrcDir = ["src", sourceSetName, "scala"].join(File.separator)
            def sourceSet = androidExtension.sourceSets."$sourceSetName"
            def scala = new DefaultSourceDirectorySet("$sourceSet.name (scala)", fileResolver)
            sourceDirectorySetMap[sourceSetName] = scala
            scala.srcDir(defaultSrcDir)
            scala.getFilter().include("**/*.scala");
            if (sourceSet.metaClass.getMetaMethod("scala")) {
                throw new GradleException('android.sourceSet.$type.scala already exists')
            }
            sourceSet.metaClass.getScala = { sourceDirectorySetMap[sourceSetName] }
            sourceSet.metaClass.setScala = { it -> sourceDirectorySetMap[sourceSetName] = it }
            sourceSet.metaClass.scala = { configureClosure ->
                ConfigureUtil.configure(configureClosure, sourceDirectorySetMap[sourceSetName])
                sourceSet.java.srcDirs = sourceSet.java.srcDirs + sourceDirectorySetMap[sourceSetName] // TODO: More clean code
                androidExtension.sourceSets
            }
            sourceSet.java.srcDir(defaultSrcDir) // for Android Studio
        }
    }

    void updateAndroidJavaCompileTask(Project project, Object androidExtension, Task task) {
        if (project.buildFile != task.project.buildFile) { // TODO: More elegant way
            return
        }
        if (!(task.name ==~ /^compile(.+)Java$/)) { // TODO: Use !=~ operator
            return
        }
        if (!(task instanceof JavaCompile)) {
            throw new GradleException("\"$task\" matches /^compile(.+)Java\$/ but is not instance of JavaCompile")
        }
        def key = (task.name ==~ /.+TestJava$/) ? "instrumentTest" : "main" // TODO: Use /TestJava$/ regexp
        task.source = task.source + sourceDirectorySetMap[key]
        def options = [target: extension.target]
        task.javaCompiler = new AndroidScalaJavaJointCompiler(project, task.javaCompiler, options)
    }

    void proguardBeforeDexDebugTestTask(Project project, Task task) {
        if (project.buildFile != task.project.buildFile) { // TODO: More elegant way
            return
        }
        if (!library || task.name != 'dexDebugTest') {
            return
        }

        def ant = project.ant
        ant.taskdef(name: 'proguard', classname: 'proguard.ant.ProGuardTask', // TODO: use properties
                classpath: project.configurations.proguardForLibraryTest.asPath)
        def jarDir = new File([project.buildDir.absolutePath, 'pre-dexed', 'test', 'debug'].join(File.separator))
        if (!jarDir.exists() || !jarDir.listFiles()) {
            throw new GradleException("Unexpected directory structure: `$jarDir' not found")
        }
        def jarFiles = jarDir.listFiles()
        def scalaLibraryJarFile = jarFiles.find { file ->
            file.name.startsWith("scala-library-") && file.name.endsWith(".jar")
        }
        if (!scalaLibraryJarFile) {
            throw new GradleException("Unexpected directory structure: `$jarDir' has no scala-library-*.jar")
        }
        def proguardDir = new File(project.buildDir, "android-scala-proguard")
        def extractDir = new File(proguardDir, "extract")
        def scalaLibraryDir = new File(proguardDir, "scala-library")
        def classpathDir = new File(proguardDir, "classpath")
        def classpathJars = []
        def scalaLibraryUndexedJar = null
        def scalaLibraryWorkDir = new File(proguardDir, "scala-library-working")
        ant.delete(dir: scalaLibraryWorkDir)
        def dexToolJar = project.configurations.proguardForLibraryTest.find { it.name.startsWith("dex-tools-") }
        ant.unzip(src: dexToolJar, dest: new File(proguardDir, "dex-tools"))
        def dex2jar = [proguardDir.absolutePath, "dex-tools", "dex2jar-" + DEX2JAR_VERSION, "d2j-dex2jar"].join(File.separator)
        def jar2dex = [proguardDir.absolutePath, "dex-tools", "dex2jar-" + DEX2JAR_VERSION, "d2j-jar2dex"].join(File.separator)
        jarFiles.each { file ->
            def destDir = new File(extractDir, file.name)
            def outputJar = new File(classpathDir, file.name)
            def isScalaLibrary = file.name.startsWith("scala-library-")
            if (isScalaLibrary) {
                ant.unzip(src: file, dest: scalaLibraryWorkDir)
                scalaLibraryUndexedJar = new File(scalaLibraryDir, file.name)
                if (scalaLibraryUndexedJar.exists()) {
                    return
                }
            } else if (outputJar.exists()) {
                return
            }
            ant.unzip(src: file, dest: destDir)
            def dex2jarCommand = []
            if (Os.isFamily(Os.FAMILY_WINDOWS)) { // TODO: more elegant way
                dex2jarCommand << dex2jar + ".bat"
            } else {
                dex2jarCommand << "/bin/sh"
                dex2jarCommand << dex2jar + ".sh"
            }
            dex2jarCommand << "-f"
            dex2jarCommand << "-o"
            if (isScalaLibrary) {
                dex2jarCommand << scalaLibraryUndexedJar.absolutePath
            } else {
                classpathJars << outputJar
                dex2jarCommand << outputJar.absolutePath
            }
            dex2jarCommand << new File(destDir, "classes.dex").absolutePath
            def out = new StringBuilder()
            def err = new StringBuilder()
            dex2jarCommand.execute().waitForProcessOutput(out, err)
            project.logger.debug("""
$dex2jarCommand
-- stdout --
$out
-- stderr --
$err
""")
        }
        def configFile = new File(proguardDir, "proguard-config.txt")
        configFile.withWriter {
            it.write """
-dontoptimize
-dontobfuscate
-dontpreverify
-dontwarn scala.**
-keep class !scala.collection.** { *; }
"""
        }

        def proguardedJar = new File(scalaLibraryUndexedJar.absolutePath + ".proguard.jar")
        ant.proguard(configuration : configFile) {
            injar(file: scalaLibraryUndexedJar)
            outjar(file: proguardedJar)
            classpathJars.each {
                libraryjar(file: it)
            }
        }

        def jar2dexCommand = []
        if (Os.isFamily(Os.FAMILY_WINDOWS)) { // TODO: more elegant way
            jar2dexCommand << jar2dex + ".bat"
        } else {
            jar2dexCommand << "/bin/sh"
            jar2dexCommand << jar2dex + ".sh"
        }
        jar2dexCommand << "-f"
        jar2dexCommand << "-o"
        jar2dexCommand << new File(scalaLibraryWorkDir, "classes.dex").absolutePath
        jar2dexCommand << proguardedJar.absolutePath
        def out = new StringBuilder()
        def err = new StringBuilder()
        jar2dexCommand.execute().waitForProcessOutput(out, err)
        project.logger.debug("""
$jar2dexCommand
-- stdout --
$out
-- stderr --
$err
""")
        ant.zip(destfile: scalaLibraryJarFile, basedir: scalaLibraryWorkDir)
    }
}
