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

import com.google.common.io.ByteStreams
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class AndroidScalaPluginSampleTask extends DefaultTask {
    @TaskAction
    def buildAndCheck() {
        [
                ["simple", ["installDebug"]],
                ["hello", ["installDebug", "connectedCheck"]],
                ["libproject", ["installDebug", "assemble", "app:connectedCheck"]],
        ].each { projectName, gradleArgs ->
            gradleArgs = ["--stacktrace", "-Pcom.android.build.threadPoolSize=5", "clean", *gradleArgs, "uninstallAll"]
            def dir = new File(project.buildFile.parentFile, "sample" + File.separator + projectName)
            def gradleWrapper = new GradleWrapper(dir)
            println "gradlew $gradleArgs"
            def process = gradleWrapper.execute(gradleArgs)
            [Thread.start { ByteStreams.copy(process.in, System.out) },
             Thread.start { ByteStreams.copy(process.err, System.err) }].each { it.join() }
            process.waitFor()
            // process.waitForProcessOutput(System.out, System.err)
            if (process.exitValue() != 0) {
                throw new IOException("process.exitValue != 0 but ${process.exitValue()}")
            }
        }
    }
}
