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
    @VisibleForTesting final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private final AndroidScalaPluginExtension extension = new AndroidScalaPluginExtension()
    private Boolean library
    private Project project
    private Object androidExtension

    @Inject
    public AndroidScalaPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    void apply(Project project, Object androidExtension) {
        this.project = project
        this.androidExtension = androidExtension
        library = !!project.plugins.findPlugin("android-library")
        updateAndroidExtension()
        updateAndroidSourceSetsExtension()
        project.gradle.taskGraph.whenReady { taskGraph ->
            addDependencies()
            taskGraph.allTasks.each { Task task ->
                updateAndroidJavaCompileTask(task)
            }
            taskGraph.beforeTask { Task task ->
                proguardBeforeDexDebugTestTask(task)
            }
        }
    }

    public void apply(Project project) {
        apply(project, project.extensions.getByName("android"))
    }

    void addDependencies() {
        def scalaLibraryDependency = project.configurations.compile.allDependencies.find {
            it.group == 'org.scala-lang' && it.name == "scala-library"
        }
        if (!scalaLibraryDependency) {
            throw new GradleException("Dependency `compile \"org.scala-lang:scala-library:\$version\"' is not defined")
        }
        def version = scalaLibraryDependency.version
        project.configurations { androidScalaPluignScalaCompiler }
        project.dependencies.add("androidScalaPluignScalaCompiler", "org.scala-lang:scala-compiler:$version")

        if (library) {
            project.repositories.maven { url "http://repository-dex2jar.forge.cloudbees.com/release/" }
            project.configurations {
                androidScalaPluginProGuard
                androidScalaPluginDexTools
            }
            project.dependencies.add("androidScalaPluginProGuard", "net.sf.proguard:proguard-anttask:4.11")
            project.dependencies.add("androidScalaPluginDexTools", "com.googlecode.dex2jar:dex-tools:$DEX2JAR_VERSION@zip")
        }
    }

    void updateAndroidExtension() {
        androidExtension.metaClass.getScala = { extension }
        androidExtension.metaClass.scala = { configureClosure ->
            ConfigureUtil.configure(configureClosure, extension)
            androidExtension
        }
    }

    void updateAndroidSourceSetsExtension() {
        ["main", "androidTest"].each { sourceSetName ->
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

    void updateAndroidJavaCompileTask(Task task) {
        if (project.buildFile != task.project.buildFile) { // TODO: More elegant way
            return
        }
        if (!(task.name ==~ /^compile(.+)Java$/)) { // TODO: Use !=~ operator
            return
        }
        if (!(task instanceof JavaCompile)) {
            throw new GradleException("\"$task\" matches /^compile(.+)Java\$/ but is not instance of JavaCompile")
        }
        def key = (task.name ==~ /.+TestJava$/) ? "androidTest" : "main" // TODO: Use /TestJava$/ regexp
        task.source = task.source + sourceDirectorySetMap[key]
        def options = [target: extension.target]
        def scalacClasspath = project.configurations.androidScalaPluignScalaCompiler.asPath
        task.javaCompiler = new AndroidScalaJavaJointCompiler(project, task.javaCompiler, options, scalacClasspath)
    }

    String getProGuardConfig() {
        '''
        -dontoptimize
        -dontobfuscate
        -dontpreverify
        -dontwarn android.**, java.**, junit.framework.**, javax.microedition.khronos.**
        -dontwarn **.R$*
        -dontwarn scala.**
        -ignorewarnings
        -keep class !scala.collection.** { *; }
        '''
    }

    void proguardBeforeDexDebugTestTask(Task task) {
        if (project.buildFile != task.project.buildFile) { // TODO: More elegant way
            return
        }
        if (!library || task.name != 'dexDebugTest') {
            return
        }
        def jarDir = new File([project.buildDir.absolutePath, 'pre-dexed', 'test', 'debug'].join(File.separator))
        if (!jarDir.isDirectory()) {
            throw new GradleException("Unexpected directory structure: `$jarDir' not found")
        }
        def libraryJars = jarDir.listFiles().toList()
        def scalaLibraryJar = libraryJars.find { file ->
            file.name.startsWith("scala-library-") && file.name.endsWith(".jar")
        }
        if (!scalaLibraryJar) {
            throw new GradleException("Unexpected directory structure: `$jarDir' has no scala-library-*.jar")
        }
        libraryJars.remove(scalaLibraryJar)

        def dexToolsZip = project.configurations.androidScalaPluginDexTools.find { it.name.startsWith("dex-tools-") }
        def unzipDir = new File(project.buildDir, "android-scala-plugin-dex-tools")
        def dexToolsDir = new File([unzipDir.absolutePath, "dex2jar-" + DEX2JAR_VERSION].join(File.separator))
        if (!dexToolsDir.isDirectory()) {
            project.ant.unzip(src: dexToolsZip, dest: unzipDir)
        }
        def dexProguard = new DexProguard(project, dexToolsDir)
        def proguardClasspath = project.configurations.androidScalaPluginProGuard.asPath
        dexProguard.execute(scalaLibraryJar, libraryJars, getProGuardConfig(), proguardClasspath)
    }
}
