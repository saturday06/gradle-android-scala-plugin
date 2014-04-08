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

/**
 * AndroidScalaPlugin adds scala language support to official gradle android plugin.
 */
public class AndroidScalaPlugin implements Plugin<Project> {
    private static final String DEX2JAR_VERSION = "0.0.9.15"
    private final FileResolver fileResolver
    @VisibleForTesting
    final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private final AndroidScalaPluginExtension extension = new AndroidScalaPluginExtension()
    private Boolean library
    private Project project
    private Object androidExtension

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
        library = !!project.plugins.findPlugin("android-library")
        updateAndroidExtension()
        updateAndroidSourceSetsExtension()
        project.gradle.taskGraph.whenReady { taskGraph ->
            addDependencies()
            taskGraph.beforeTask { Task task ->
                updateAndroidJavaCompileTask(task)
                proguardBeforeDexDebugTestTask(task)
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
        if (!library) {
            return
        }
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
        -dontwarn android.**
        -dontwarn java.**
        -dontwarn javax.microedition.khronos.**
        -dontwarn junit.framework.**
        -dontwarn scala.**
        -dontwarn **.R$*
        -dontnote android.**
        -dontnote scala.**
        -ignorewarnings
        -keep class !scala.collection.** { *; }
        '''
    }

    /**
     * Executes proguard for pre-dexed jars.
     *
     * @param task the dexDebugTest task
     */
    void proguardBeforeDexDebugTestTask(Task task) {
        if (project.buildFile != task.project.buildFile) { // TODO: More elegant way
            return
        }
        if (!library || !task.name.startsWith("dex") || !(task.metaClass.respondsTo(task, 'variant'))) {
            return
        }
        def variant = task.variant
        if (variant?.class?.simpleName != "TestVariantData") { // TODO: More elegant way
            return
        }
        def flavorName = variant.variantConfiguration.flavorName
        def buildTypeName = variant.variantConfiguration.buildType.name
        def jarDir = new File([project.buildDir.absolutePath, 'pre-dexed', 'test', flavorName, buildTypeName].join(File.separator))
        if (!jarDir.isDirectory()) {
            throw new GradleException("Unexpected directory structure: `$jarDir' not found")
        }
        def libraryJars = jarDir.listFiles().toList()
        def scalaLibraryJar = libraryJars.find { file ->
            file.name.startsWith("scala-library-") && file.name.endsWith(".jar")
        }
        if (!scalaLibraryJar) {
            return
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
