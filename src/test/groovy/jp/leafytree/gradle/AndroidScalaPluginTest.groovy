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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AndroidScalaPluginTest {
    Project project

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: androidPluginName()
    }

    public String androidPluginName() {
        "android"
    }

    @Test
    public void applyingBeforeAndroidPluginShouldThrowException() {
        project = ProjectBuilder.builder().build()
        try {
            project.apply plugin: "jp.leafytree.android-scala"
            Assert.fail("Should throw Exception")
        } catch (GradleException e) {
        }
    }

    @Test
    public void applyingAfterAndroidPluginShouldNeverThrowException() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: androidPluginName()
        project.apply plugin: "jp.leafytree.android-scala" // never throw Exception
    }

    def getPlugin() {
        project.apply plugin: "jp.leafytree.android-scala"
        project.plugins.findPlugin(AndroidScalaPlugin.class)
    }

    @Test
    public void addDefaultScalaMainSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList())
        def src1 = new File(project.file("."), ["src", "main", "scala", "Src1.scala"].join(File.separator))
        src1.parentFile.mkdirs()
        src1.withWriter { it.write("class Src1{}") }
        Assert.assertEquals([src1], plugin.sourceDirectorySetMap["main"].files.toList())
        Assert.assertEquals([], plugin.sourceDirectorySetMap["androidTest"].files.toList())
    }

    @Test
    public void addCustomFlavorScalaSourceSetToAndroidPlugin() {
        project.android { productFlavors { customFlavor { } } }
        def plugin = getPlugin()
        Assert.assertEquals([], plugin.sourceDirectorySetMap["customFlavor"].files.toList())

        def src = new File(project.file("."), ["src", "customFlavor", "scala", "Src.scala"].join(File.separator))
        src.parentFile.mkdirs()
        src.withWriter { it.write("class Src{}") }

        def testSrc = new File(project.file("."), ["src", "androidTestCustomFlavor", "scala", "TestSrc.scala"].join(File.separator))
        testSrc.parentFile.mkdirs()
        testSrc.withWriter { it.write("class TestSrc{}") }

        Assert.assertEquals([src], plugin.sourceDirectorySetMap["customFlavor"].files.toList())
        Assert.assertEquals([testSrc], plugin.sourceDirectorySetMap["androidTestCustomFlavor"].files.toList())
    }

    @Test
    public void addDefaultScalaAndroidTestSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        Assert.assertEquals([], plugin.sourceDirectorySetMap["androidTest"].files.toList())
        def src1 = new File(project.file("."), ["src", "androidTest", "scala", "Src1Test.scala"].join(File.separator))
        src1.parentFile.mkdirs()
        src1.withWriter { it.write("class Src1Test{}") }
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList())
        Assert.assertEquals([src1], plugin.sourceDirectorySetMap["androidTest"].files.toList())
    }

    @Test
    public void addCustomScalaMainSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def defaultSrc = new File(project.file("."), ["src", "main", "scala", "Src1.scala"].join(File.separator))
        def customSrc = new File(project.file("."), ["custom", "sourceSet", "Src2.scala"].join(File.separator))
        defaultSrc.parentFile.mkdirs()
        defaultSrc.withWriter { it.write("class Src1{}") }
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2{}") }
        project.android { sourceSets { main { scala { srcDir "custom/sourceSet" } } } }
        Assert.assertEquals([customSrc, defaultSrc], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([], plugin.sourceDirectorySetMap["androidTest"].files.toList().sort())
    }

    @Test
    public void addCustomScalaAndroidTestSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def defaultSrc = new File(project.file("."), ["src", "androidTest", "scala", "Src1.scala"].join(File.separator))
        def customSrc = new File(project.file("."), ["custom", "sourceSet", "Src2.scala"].join(File.separator))
        defaultSrc.parentFile.mkdirs()
        defaultSrc.withWriter { it.write("class Src1{}") }
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2{}") }
        project.android { sourceSets { androidTest { scala { srcDir "custom/sourceSet" } } } }
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([customSrc, defaultSrc], plugin.sourceDirectorySetMap["androidTest"].files.toList().sort())
    }

    @Test
    public void updateCustomScalaMainSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def customSrc = new File(project.file("."), ["custom", "sourceSet", "Src2.scala"].join(File.separator))
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2{}") }
        project.android { sourceSets { main { scala { srcDirs = ["custom/sourceSet"] } } } }
        Assert.assertEquals([customSrc], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([], plugin.sourceDirectorySetMap["androidTest"].files.toList().sort())
    }

    @Test
    public void updateCustomScalaAndroidTestSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def customSrc = new File(project.file("."), ["custom", "testSourceSet", "Src1Test.scala"].join(File.separator))
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2Test{}") }
        project.android { sourceSets { androidTest { scala { srcDirs = ["custom/testSourceSet"] } } } }
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([customSrc], plugin.sourceDirectorySetMap["androidTest"].files.toList().sort())
    }

    @Test
    public void updateCustomScalaUnitTestSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def customSrc = new File(project.file("."), ["custom", "testSourceSet", "Src1Test.scala"].join(File.separator))
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2Test{}") }
        project.android { sourceSets { test { scala { srcDirs = ["custom/testSourceSet"] } } } }
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([customSrc], plugin.sourceDirectorySetMap["test"].files.toList().sort())
    }

    @Test
    public void scalaVersionFromClasspath() {
        def classpath = System.getProperty("java.class.path").split(File.pathSeparator).collect { new File(it) }
        Assert.assertEquals("2.11.7", AndroidScalaPlugin.scalaVersionFromClasspath(classpath))
    }
}
