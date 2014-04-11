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
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 * AndroidScalaPlugin adds scala language support to official gradle android plugin.
 */
public class AndroidScalaPlugin implements Plugin<Project> {
    private static final String DEX2JAR_VERSION = "0.0.9.15"
    private final FileResolver fileResolver
    @VisibleForTesting
    final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private final AndroidScalaPluginExtension extension = new AndroidScalaPluginExtension()
    private Project project
    private Object androidExtension
    private Class dexClass
    private Class testVariantDataClass

    /**
     * Creates a new AndroidScalaJavaJointCompiler with given file resolver.
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
        this.androidExtension = androidExtension
        def classLoader = androidExtension.class.classLoader
        dexClass = classLoader.loadClass("com.android.build.gradle.tasks.Dex")
        testVariantDataClass = classLoader.loadClass("com.android.build.gradle.internal.variant.TestVariantData")
        updateAndroidExtension()
        updateAndroidSourceSetsExtension()
        project.gradle.taskGraph.whenReady { taskGraph ->
            addDependencies()
            taskGraph.beforeTask { Task task ->
                updateAndroidJavaCompileTask(task)
                proguardBeforeDexTestTask(task)
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
        apply(project, project.extensions.getByName("android"))
    }

    /**
     * Adds dependencies to execute plugin.
     */
    void addDependencies() {
        project.repositories.maven { url "http://repository-dex2jar.forge.cloudbees.com/release/" }
        project.configurations {
            androidScalaPluginProGuard
            androidScalaPluginDexTools
        }
        project.dependencies.add("androidScalaPluginProGuard", "net.sf.proguard:proguard-anttask:4.11")
        project.dependencies.add("androidScalaPluginDexTools", "com.googlecode.dex2jar:dex-tools:$DEX2JAR_VERSION@zip")
    }

    /**
     * Returns scala version from scala-library in given classpath.
     *
     * @param classpath the classpath contains scala-library
     * @return scala version
     */
    static String scalaVersionFromClasspath(String classpath) {
        def urls = new ArrayList<URL>()
        for (String path : classpath.split(File.pathSeparator)) {
            urls.add(new File(path).toURI().toURL())
        }
        def classLoader = new URLClassLoader(urls.toArray(new URL[0]))
        def properties
        try {
            properties = classLoader.loadClass("scala.util.Properties\$")
        } catch (ClassNotFoundException e) {
            return null
        }
        def versionNumber = properties.MODULE$.scalaProps["maven.version.number"]
        return versionNumber
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
            sourceSet.convention.plugins.scala = new AndroidScalaSourceSet(sourceSet.displayName, fileResolver)
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
     * @param task the Task to update
     */
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
        def scalaVersion = scalaVersionFromClasspath(task.classpath.asPath)
        if (!scalaVersion) {
            return;
        }

        def options = [target: extension.target]
        def configurationName = "androidScalaPluginScalaCompilerFor" + task.name
        def configuration = project.configurations.findByName(configurationName)
        if (!configuration) {
            configuration = project.configurations.create(configurationName)
            project.dependencies.add(configurationName, "org.scala-lang:scala-compiler:$scalaVersion")
        }
        task.javaCompiler = new AndroidScalaJavaJointCompiler(project, task.javaCompiler, options, configuration.asPath)
    }

    /**
     * Returns the proguard configuration text.
     *
     * @return the proguard configuration text
     */
    String getProGuardConfig() {
        '''
        -dontoptimize
        -dontobfuscate
        -dontpreverify
        -dontwarn android.**, java.**, javax.microedition.khronos.**, junit.framework.**, scala.**, **.R$*
        -dontnote android.**, java.**, javax.microedition.khronos.**, junit.framework.**, scala.**, **.R$*
        -ignorewarnings
        -keep class !scala.** { *; }
        -keep class scala.beans.ScalaBeanInfo
        '''
    }

    /**
     * Executes proguard before task.Dex
     *
     * @param task the dexDebugTest task
     * @param workDir working directory
     * @param jars target jars
     */
    void proguardBeforeDexApplicationTestTask(Task task, File workDir, List<File> jars) {
        def outputJar = jars.find { it.name == "classes.jar" }
        if (!outputJar) {
            project.logger.error("class.jar is not found in tasks.Dex.inputs.files ($task.inputs.files)")
            return
        }
        def tempOutputDir = new File(workDir, "proguard-classes")
        def tempOutputJar = new File(tempOutputDir, outputJar.name)
        FileUtils.deleteDirectory(tempOutputDir)
        def ant = new AntBuilder()
        ant.taskdef(name: 'proguard', classname: 'proguard.ant.ProGuardTask', // TODO: use properties
                classpath: project.configurations.androidScalaPluginProGuard.asPath)
        def proguardConfigFile = new File(project.buildDir, "proguard-config-app.txt")
        proguardConfigFile.withWriter { it.write getProGuardConfig() }
        ant.proguard(configuration: proguardConfigFile) {
            jars.each {
                injar(file: it)
            }
            outjar(file: tempOutputDir)
        }
        ant.copy(file: tempOutputJar, toFile: outputJar)
    }

    /**
     * Executes proguard before task.Dex
     *
     * @param task the dexDebugTest task
     * @param workDir working directory
     * @param jars target jars
     */
    void proguardBeforeDexLibraryTestTask(Task task, File workDir, List<File> jars) {
        def scalaLibraryJar = jars.find { it.name.startsWith("scala-library-") }
        if (!scalaLibraryJar) {
            return
        }
        jars.remove(scalaLibraryJar)
        def dexToolsZip = project.configurations.androidScalaPluginDexTools.find { it.name.startsWith("dex-tools-") }
        def unzipDir = new File(workDir, "dex-tools")
        def dexToolsDir = new File([unzipDir.absolutePath, "dex2jar-" + DEX2JAR_VERSION].join(File.separator))
        if (!dexToolsDir.isDirectory()) {
            project.ant.unzip(src: dexToolsZip, dest: unzipDir)
        }
        def dexProguard = new DexProguard(workDir, dexToolsDir, project.logger)
        def proguardClasspath = project.configurations.androidScalaPluginProGuard.asPath
        dexProguard.execute(scalaLibraryJar, jars, getProGuardConfig(), proguardClasspath)
    }

    /**
     * Executes proguard before task.Dex
     *
     * @param task the dexDebugTest task
     */
    void proguardBeforeDexTestTask(Task task) {
        if (project.buildFile != task.project.buildFile) { // TODO: More elegant way
            return
        }
        if (!dexClass.isInstance(task)) {
            return
        }
        def variant = task.variant
        if (!testVariantDataClass.isInstance(variant)) {
            return
        }
        def jars = task.inputs.files.findAll { it.name.endsWith(".jar") }
        if (jars.empty) {
            return
        }
        def workDir = new File([project.buildDir, "android-scala", variant.name].join(File.separator))
        if (project.plugins.hasPlugin("android")) {
            proguardBeforeDexApplicationTestTask(task, workDir, jars)
        } else {
            proguardBeforeDexLibraryTestTask(task, workDir, jars)
        }
    }
}
