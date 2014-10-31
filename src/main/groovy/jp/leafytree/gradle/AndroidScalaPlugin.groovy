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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.tasks.DefaultScalaSourceSet
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.util.ConfigureUtil

import javax.inject.Inject
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
        updateAndroidExtension()
        updateAndroidSourceSetsExtension()
        androidExtension.buildTypes.whenObjectAdded { updateAndroidSourceSetsExtension() }
        androidExtension.productFlavors.whenObjectAdded { updateAndroidSourceSetsExtension() }
        androidExtension.signingConfigs.whenObjectAdded { updateAndroidSourceSetsExtension() }

        project.afterEvaluate {
            updateAndroidSourceSetsExtension()
            androidExtension.sourceSets.each { it.java.srcDirs(it.scala.srcDirs) }
            def allVariants = androidExtension.testVariants + (isLibrary ? androidExtension.libraryVariants : androidExtension.applicationVariants)
            allVariants.each { variant ->
                addAndroidScalaCompileTask(variant)
            }
        }

        project.tasks.findByName("preBuild").doLast {
            FileUtils.forceMkdir(workDir)
        }
    }

    /**
     * Registers the plugin to current project.
     *
     * @param project currnet project
     * @param androidExtension extension of Android Plugin
     */
    public void apply(Project project) {
        if (!["com.android.application", "android", "com.android.library", "android-library"].any { project.plugins.findPlugin(it) }) {
            throw new ProjectConfigurationException("Please apply 'com.android.application' or 'com.android.library' plugin before applying 'android-scala' plugin", null)
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
     * Returns scala version from scala-library in given classpath.
     *
     * @param classpath the classpath contains scala-library
     * @return scala version
     */
    static String scalaVersionFromClasspath(Collection<File> classpath) {
        def urls = classpath.collect { it.toURI().toURL() }
        def classLoader = new URLClassLoader(urls.toArray(new URL[0]))
        try {
            def propertiesClass
            try {
                propertiesClass = classLoader.loadClass("scala.util.Properties\$")
            } catch (ClassNotFoundException e) {
                return null
            }
            def versionNumber = propertiesClass.MODULE$.scalaProps["maven.version.number"]
            return new String(versionNumber) // Remove reference from ClassLoader
        } finally {
            if (classLoader instanceof Closeable) {
                classLoader.close()
            }
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
            if (sourceDirectorySetMap.containsKey(sourceSet.name)) {
                return
            }
            def include = "**/*.scala"
            sourceSet.java.filter.include(include);
            sourceSet.convention.plugins.scala = new DefaultScalaSourceSet(sourceSet.name + "_AndroidScalaPlugin", fileResolver)
            def scala = sourceSet.scala
            scala.filter.include(include);
            def scalaSrcDir = ["src", sourceSet.name, "scala"].join(File.separator)
            scala.srcDir(scalaSrcDir)
            sourceDirectorySetMap[sourceSet.name] = scala
        }
    }

    /**
     * Updates AndroidPlugin's compilation task to support scala.
     *
     * @param task the JavaCompile task
     */
    void addAndroidScalaCompileTask(Object variant) {
        def javaCompileTask = variant.javaCompile
        // To prevent locking classes.jar by JDK6's URLClassLoader
        def libraryClasspath = javaCompileTask.classpath.grep { it.name != "classes.jar" }
        def scalaVersion = scalaVersionFromClasspath(libraryClasspath)
        if (!scalaVersion) {
            return
        }
        project.logger.info("scala-library version=$scalaVersion detected")
        def configurationName = "androidScalaPluginScalaCompilerFor" + javaCompileTask.name
        def configuration = project.configurations.findByName(configurationName)
        if (!configuration) {
            configuration = project.configurations.create(configurationName)
            project.dependencies.add(configurationName, "org.scala-lang:scala-compiler:$scalaVersion")
            project.dependencies.add(configurationName, "com.typesafe.zinc:zinc:0.3.5")
        }
        def variantWorkDir = getVariantWorkDir(variant)
        def destinationDir = new File(variantWorkDir, "scalaCompile") // TODO: More elegant way
        def scalaCompileTask = project.tasks.create("compile${variant.name.capitalize()}Scala", ScalaCompile)
        def scalaSources = variant.variantData.variantConfiguration.sortedSourceProviders.inject([]) { acc, val ->
            acc + val.java.sourceFiles
        }
        scalaCompileTask.source = [] + new HashSet(scalaSources + javaCompileTask.source) // unique
        scalaCompileTask.destinationDir = destinationDir
        scalaCompileTask.sourceCompatibility = javaCompileTask.sourceCompatibility
        scalaCompileTask.targetCompatibility = javaCompileTask.targetCompatibility
        scalaCompileTask.scalaCompileOptions.encoding = javaCompileTask.options.encoding
        scalaCompileTask.options.encoding = javaCompileTask.options.encoding
        scalaCompileTask.options.bootClasspath = androidPlugin.bootClasspath.join(File.pathSeparator)
        // TODO: Remove bootClasspath
        scalaCompileTask.classpath = javaCompileTask.classpath + project.files(androidPlugin.bootClasspath)
        scalaCompileTask.scalaClasspath = configuration.asFileTree
        scalaCompileTask.zincClasspath = configuration.asFileTree
        scalaCompileTask.scalaCompileOptions.incrementalOptions.analysisFile = new File(variantWorkDir, "analysis.txt")
        if (extension.addparams) {
            scalaCompileTask.scalaCompileOptions.additionalParameters = [extension.addparams]
        }
        scalaCompileTask.doFirst {
            FileUtils.forceMkdir(destinationDir)
            // R.java is appended lazily
            scalaCompileTask.source = [] + new HashSet([] + scalaCompileTask.source + javaCompileTask.source) // unique
        }
        javaCompileTask.dependsOn.each {
            scalaCompileTask.dependsOn it
        }
        javaCompileTask.classpath = javaCompileTask.classpath + project.files(destinationDir)
        javaCompileTask.dependsOn scalaCompileTask
        javaCompileTask.doLast {
            project.ant.copy(todir: javaCompileTask.destinationDir, preservelastmodified: true) {
                fileset(dir: destinationDir)
            }
        }
    }
}
