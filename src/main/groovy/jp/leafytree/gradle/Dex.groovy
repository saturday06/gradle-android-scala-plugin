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

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger

public class Dex {
    public final AntBuilder ant
    public final File workDir
    public final File dexToolsDir
    public final Logger logger

    public Dex(Project project, File dexToolsDir) {
        this(project.ant, new File(project.buildDir, "android-scala-plugin-dex"), dexToolsDir, project.logger)
    }

    public Dex(AntBuilder ant, File workDir, File dexToolsDir, Logger logger) {
        this.ant = ant
        this.workDir = workDir
        this.dexToolsDir = dexToolsDir
        this.logger = logger
    }

    public File undex(File file) {
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

    public void proguard(File dexJar, List<File> dependencyDexJars, String config, String proguardClasspath = null) {
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

    File mkdirsOrThrow(String... path) {
        def dir = new File(([workDir] + path.toList()).join(File.separator))
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new GradleException("Directory `$dir' couldn't be created")
        }
        dir
    }

    void executeDexTools(String toolName, List options) {
        def command = []
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            command << "$dexToolsDir$File.separator${toolName}.bat"
        } else {
            command += ["/bin/sh", "$dexToolsDir$File.separator${toolName}.sh"]
        }
        command += options
        command = command.collect { it.toString() }.toList() // TODO: what's this ?
        def processBuilder = new ProcessBuilder(command)
        processBuilder.directory(dexToolsDir)
        def process = processBuilder.start()
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()
        process.waitForProcessOutput(stdout, stderr)
        logger.debug("""
-- command --
$command
-- stdout ---
$stdout
-- stderr ---
$stderr
""")
        if (process.exitValue() != 0) {
            throw new GradleException("process.exitValue != 0 but ${process.exitValue()}")
        }
    }
}
