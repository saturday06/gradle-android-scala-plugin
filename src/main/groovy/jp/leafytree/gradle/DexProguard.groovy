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
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.logging.Logger

/**
 * Proguard utilities for dex.
 */
class DexProguard {
    private final AntBuilder ant = new AntBuilder()
    private final File workDir
    private final File dexToolsDir
    private final Logger logger

    /**
     * Creates a new DexProguard with given parameters.
     *
     * @param workDir the directory to put temporary files
     * @param dexToolsDir the directory contains dex tools
     * @param logger the Logger to use
     */
    DexProguard(File workDir, File dexToolsDir, Logger logger) {
        this.workDir = workDir
        this.dexToolsDir = dexToolsDir
        this.logger = logger
    }

    /**
     * Extracts dex.
     *
     * @param file dex file
     * @return extracted jar
     */
    File undex(File file) {
        def undexedJar = new File(mkdirsOrThrow("un-dexed"), file.name)
        if (undexedJar.exists()) {
            return undexedJar
        }
        def destDir = mkdirsOrThrow("un-dexed-extract", file.name)
        ant.unzip(src: file, dest: destDir)
        executeDexTools("d2j-dex2jar",
                ["-f", "-o", undexedJar.absolutePath, new File(destDir, "classes.dex").absolutePath])
        undexedJar
    }

    /**
     * Executes proguard.
     *
     * @param dexJar the jar file contains dex
     * @param dependencyDexJars dependency files of dexJar
     * @param config configuration for proguard
     * @param proguardClasspath classpath of proguard
     */
    void execute(File dexJar, List<File> dependencyDexJars, String config, String proguardClasspath = null) {
        // extract jars
        def extractDir = new File(workDir, "proguard-extract" + File.separator + dexJar.name)
        if (!extractDir.exists()) {
            ant.unzip(src: dexJar, dest: extractDir)
        }
        def jar = undex(dexJar)
        def dependencyJars = dependencyDexJars.collect { undex(it) }

        // proguard
        def proguardDir = mkdirsOrThrow("proguard")
        def proguardedJar = new File(proguardDir, jar.name)
        ant.taskdef(name: 'proguard', classname: 'proguard.ant.ProGuardTask', // TODO: use properties
                classpath: proguardClasspath)
        def proguardConfigFile = new File(workDir, "proguard-config.txt")
        proguardConfigFile.withWriter { it.write config }
        ant.proguard(configuration: proguardConfigFile) {
            injar(file: jar)
            dependencyJars.each {
                injar(file: it)
            }
            outjar(file: proguardDir)
        }

        // replace dexJar with proguard-ed one
        executeDexTools("d2j-jar2dex", [
                "-f", "-o", new File(extractDir, "classes.dex").absolutePath, proguardedJar.absolutePath
        ])
        ant.zip(destfile: dexJar, basedir: extractDir)
    }

    /**
     * Makes directory or throw GradleException.
     *
     * @param pathElements the path elements
     * @return created directory
     * @throws GradleException if the directory cannot be created
     */
    File mkdirsOrThrow(String... pathElements) throws GradleException {
        def dir = new File(([workDir] + pathElements.toList()).join(File.separator))
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new GradleException("Directory `$dir' couldn't be created")
        }
        dir
    }

    /**
     * Executes tools in dex-tools.
     *
     * @param toolName the tool name in dex-tools
     * @param options options to pass
     */
    void executeDexTools(String toolName, List options) {
        def ext = Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : ".sh"
        def script = dexToolsDir.absolutePath + File.separator + toolName + ext
        new File(script).setExecutable(true)
        def command = [script] + options
        def processBuilder = new ProcessBuilder(command)
        processBuilder.directory(dexToolsDir)
        def process = processBuilder.start()
        Thread.start { ByteStreams.copy(process.in, System.out) }
        Thread.start { ByteStreams.copy(process.err, System.err) }
        // process.waitForProcessOutput(System.out, System.err)
        process.waitFor()
        if (process.exitValue() != 0) {
            throw new GradleException("process.exitValue != 0 but ${process.exitValue()}")
        }
    }
}
