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
    private final FileResolver fileResolver
    private final Map<String, SourceDirectorySet> sourceDirectorySetMap = new HashMap<>()
    private final AndroidScalaPluginExtension extension = new AndroidScalaPluginExtension()

    @Inject
    public AndroidScalaPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    public void apply(Project project) {
        def androidExtension = project.extensions.getByName("android")
        updateAndroidExtension(project, androidExtension)
        updateAndroidSourceSetsExtension(project, androidExtension)
        project.gradle.taskGraph.whenReady { taskGraph ->
            addScalaDependencies(project)
            taskGraph.allTasks.each { Task task ->
                updateAndroidJavaCompileTask(project, androidExtension, task)
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
}
