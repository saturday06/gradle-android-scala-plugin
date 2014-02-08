package jp.leafytree.android

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.apache.tools.ant.taskdefs.condition.Os

public class AndroidScalaPluginEndToEndTestTask extends DefaultTask {
    @TaskAction
    def run() {
        [
                ["app", ["installDebug", "connectedInstrumentTest"]],
                ["lib", ["connectedInstrumentTest"]],
                ["appAndLib", ["installDebug", "connectedInstrumentTest"]],
        ].each { projectArgs ->
            [
                    ["1.10", "2.10.3", "0.8.1", 19, "19.0.1", 4, 19],
                    ["1.10", "2.10.3", "0.8.1", 19, "19.0.1", 7, 19],
            ].each { buildArgs ->
                runProject(* (projectArgs + buildArgs))
            }
        }
    }

    def runProject(projectName, tasks, gradleVersion, scalaLibraryVersion, androidPluginVersion, androidPluginCompileSdkVersion,
                   androidPluginBuildToolsVersion, androidPluginMinSdkVersion, androidPluginTargetSdkVersion) {
        // def snaphotRepositoryUrl = "http://saturday06.github.io/gradle-android-scala-plugin/repository/snapshot"
        def snaphotRepositoryUrl = [project.buildFile.parentFile.absolutePath, "gh-pages", "repository", "snapshot"].join(File.separator)
        snaphotRepositoryUrl = snaphotRepositoryUrl.replaceAll('\\\\', '\\\\\\\\')
        def baseDir = new File([project.buildFile.parentFile.absolutePath, "src", "endToEndTest"].join(File.separator))
        def projectDir = new File([baseDir.absolutePath, "project", projectName].join(File.separator))
        new File(baseDir, ["gradle", "wrapper", "gradle-wrapper.properties"].join(File.separator)).withWriter {
            it.write("""
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=http\\://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip
""")
        }

        new File(projectDir, "gradle.properties").withWriter {
            it.write("""
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=1024m -XX:+HeapDumpOnOutOfMemoryError
snaphotRepositoryUrl=$snaphotRepositoryUrl
scalaLibraryVersion=$scalaLibraryVersion
androidScalaTarget=jvm-1.6
androidScalaPluginVersion=1.0-SNAPSHOT
androidPluginVersion=$androidPluginVersion
androidPluginCompileSdkVersion=$androidPluginCompileSdkVersion
androidPluginBuildToolsVersion=$androidPluginBuildToolsVersion
androidPluginMinSdkVersion=$androidPluginMinSdkVersion
androidPluginTargetSdkVersion=$androidPluginTargetSdkVersion
""")
        }
        def command = []
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            command << new File(baseDir, "gradlew.bat").absolutePath
        } else {
            command << "/bin/sh"
            command << new File(baseDir, "gradlew").absolutePath
        }
        command << "--stacktrace"
        command << "--project-dir"
        command << projectDir.absolutePath
        command += tasks
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
