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
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.compile.Compiler
import org.gradle.api.internal.tasks.compile.JavaCompileSpec
import org.gradle.api.tasks.WorkResult

class AndroidScalaJavaJointCompiler implements Compiler<JavaCompileSpec> {
    private final Project project
    private final Compiler<JavaCompileSpec> androidJavaCompiler
    private final Map extraOptions

    public AndroidScalaJavaJointCompiler(Project project, Compiler<JavaCompileSpec> androidJavaCompiler, Map extraOptions) {
        this.project = project
        this.androidJavaCompiler = androidJavaCompiler
        this.extraOptions = extraOptions
    }

    public WorkResult execute(JavaCompileSpec spec) {
        // scala
        def scalacDestinationDir = new File(spec.destinationDir.getAbsolutePath() + "-scala")
        if (!scalacDestinationDir.exists() && !scalacDestinationDir.mkdirs()) {
            throw new GradleException("Failed to create directory: $scalacDestinationDir")
        }
        def options = spec.compileOptions.optionMap().findAll {
            [
                    "srcdir", "classpath", "sourcepath", "bootclasspath",
                    "extdirs", "target", "deprecation", "encoding", "failonerror",
            ].contains(it.key)
        }
        if (!options["bootclasspath"] && spec.compileOptions.bootClasspath) {
            options["bootclasspath"] = spec.compileOptions.bootClasspath
        }
        options += extraOptions
        def ant = project.ant
        ant.taskdef(name: 'scalac', classname: 'scala.tools.ant.Scalac', // TODO: import antlib xml
                classpath: project.configurations.scalaCompileProvided.asPath)
        options["destDir"] = scalacDestinationDir
        ant.scalac(options) {
            project.configurations.scalaCompileProvided.each { file ->
                bootclasspath(location: file)
            }
            spec.classpath.each { file ->
                classpath(location: file)
            }
            spec.source.addToAntBuilder(ant, 'src', FileCollection.AntType.MatchingTask)
        }

        // java
        spec.source = spec.source.filter { !it.absolutePath.endsWith(".scala") }
        if (spec.source.empty) {
            return { true } as WorkResult
        }
        spec.classpath = spec.classpath + project.files(scalacDestinationDir)
        def result = androidJavaCompiler.execute(spec)

        // restore compiled scala classes
        ant.copy(todir: spec.destinationDir) {
            fileset(dir: scalacDestinationDir)
        }
        return result
    }
}
