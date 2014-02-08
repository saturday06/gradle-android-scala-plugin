package jp.leafytree.android

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class AndroidScalaPluginEndToEndTestTask extends DefaultTask {
    @TaskAction
    def run() {
        [
                ["app", ["installDebug", "connectedInstrumentTest"]],
                ["lib", ["connectedInstrumentTest"]],
                ["appAndLib", ["installDebug", "connectedInstrumentTest"]],
        ].each { projectArgs ->
            [
                    ["1.10", "2.10.3", "0.8.1", "19", "19.0.1", "4", "19"],
                    ["1.10", "2.10.3", "0.8.1", "19", "19.0.1", "7", "19"],
            ].each { buildArgs ->
                def gradleVersion = buildArgs.first()
                def gradleWrapperProperties = getGradleWrapperProperties(gradleVersion)
                def gradleProperties = getGradleProperties(buildArgs.drop(1))
                println "Test $projectArgs gradleVersion:$gradleVersion $gradleProperties"
                runProject(projectArgs[0], projectArgs[1], gradleWrapperProperties, gradleProperties)
            }
        }
    }

    def getGradleWrapperProperties(gradleVersion) {
        def gradleWrapperProperties = new Properties()
        gradleWrapperProperties.putAll([
                distributionBase: "GRADLE_USER_HOME",
                distributionPath: "wrapper/dists",
                zipStoreBase: "GRADLE_USER_HOME",
                zipStorePath: "wrapper/dists",
                distributionUrl: "http://services.gradle.org/distributions/gradle-" + gradleVersion + "-bin.zip",
        ])
        gradleWrapperProperties
    }

    def getGradleProperties(scalaLibraryVersion, androidPluginVersion, androidPluginCompileSdkVersion,
                            androidPluginBuildToolsVersion, androidPluginMinSdkVersion, androidPluginTargetSdkVersion) {
        // def snaphotRepositoryUrl = "http://saturday06.github.io/gradle-android-scala-plugin/repository/snapshot"
        def snaphotRepositoryUrl = [project.buildFile.parentFile.absolutePath, "gh-pages", "repository", "snapshot"].join(File.separator)
        def gradleProperties = new Properties()
        gradleProperties.putAll([
                "org.gradle.jvmargs": "-Xmx2048m -XX:MaxPermSize=1024m -XX:+HeapDumpOnOutOfMemoryError",
                snaphotRepositoryUrl: snaphotRepositoryUrl,
                scalaLibraryVersion: scalaLibraryVersion,
                androidScalaTarget: "jvm-1.6",
                androidScalaPluginVersion: "1.0-SNAPSHOT",
                androidPluginVersion: androidPluginVersion,
                androidPluginCompileSdkVersion: androidPluginCompileSdkVersion,
                androidPluginBuildToolsVersion: androidPluginBuildToolsVersion,
                androidPluginMinSdkVersion: androidPluginMinSdkVersion,
                androidPluginTargetSdkVersion: androidPluginTargetSdkVersion,
        ])
        gradleProperties
    }

    def runProject(projectName, tasks, gradleWrapperProperties, gradleProperties) {
        def baseDir = new File([project.buildFile.parentFile.absolutePath, "src", "endToEndTest"].join(File.separator))
        def projectDir = new File([baseDir.absolutePath, "project", projectName].join(File.separator))
        new File(baseDir, ["gradle", "wrapper", "gradle-wrapper.properties"].join(File.separator)).withWriter {
            gradleWrapperProperties.store(it, getClass().getName())
        }
        new File(projectDir, "gradle.properties").withWriter {
            gradleProperties.store(it, getClass().getName())
        }

        def command = Os.isFamily(Os.FAMILY_WINDOWS) ?
                [new File(baseDir, "gradlew.bat").absolutePath] :
                ["/bin/sh", new File(baseDir, "gradlew").absolutePath]
        command += ["--stacktrace", "--project-dir", projectDir.absolutePath] + tasks
        def stdout = new StringBuilder()
        def stderr = new StringBuilder()
        def process = command.execute()
        process.waitForProcessOutput(stdout, stderr)
        println("""
-- command --
$command
-- stdout ---
$stdout
-- stderr ---
$stderr
""")
        if (process.exitValue() != 0) {
            throw new IOException("process.exitValue != 0 but ${process.exitValue()}")
        }
    }
}
