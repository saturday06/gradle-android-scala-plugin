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
    }

    @Test
    public void applyingBeforeAndroidPluginShouldThrowException() {
        try {
            project.apply plugin: 'android-scala'
            Assert.fail("Should throw Exception")
        } catch (GradleException e) {
        }
    }

    @Test
    public void applyingAfterAndroidPluginShouldNeverThrowException() {
        project.apply plugin: 'android'
        project.apply plugin: 'android-scala' // never throw Exception
    }

    @Test
    public void applyingAfterAndroidLibraryPluginShouldNeverThrowException() {
        project.apply plugin: 'android-library'
        project.apply plugin: 'android-scala' // never throw Exception
    }

    def getPlugin() {
        project.apply plugin: 'android'
        project.apply plugin: 'android-scala'
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
        Assert.assertEquals([], plugin.sourceDirectorySetMap["instrumentTest"].files.toList())
    }

    @Test
    public void addDefaultScalaInstrumentTestSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        Assert.assertEquals([], plugin.sourceDirectorySetMap["instrumentTest"].files.toList())
        def src1 = new File(project.file("."), ["src", "instrumentTest", "scala", "Src1Test.scala"].join(File.separator))
        src1.parentFile.mkdirs()
        src1.withWriter { it.write("class Src1Test{}") }
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList())
        Assert.assertEquals([src1], plugin.sourceDirectorySetMap["instrumentTest"].files.toList())
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
        Assert.assertEquals([], plugin.sourceDirectorySetMap["instrumentTest"].files.toList().sort())
    }

    @Test
    public void addCustomScalaInstrumentTestSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def defaultSrc = new File(project.file("."), ["src", "instrumentTest", "scala", "Src1.scala"].join(File.separator))
        def customSrc = new File(project.file("."), ["custom", "sourceSet", "Src2.scala"].join(File.separator))
        defaultSrc.parentFile.mkdirs()
        defaultSrc.withWriter { it.write("class Src1{}") }
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2{}") }
        project.android { sourceSets { instrumentTest { scala { srcDir "custom/sourceSet" } } } }
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([customSrc, defaultSrc], plugin.sourceDirectorySetMap["instrumentTest"].files.toList().sort())
    }

    @Test
    public void updateCustomScalaMainSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def customSrc = new File(project.file("."), ["custom", "sourceSet", "Src2.scala"].join(File.separator))
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2{}") }
        project.android { sourceSets { main { scala { srcDirs = ["custom/sourceSet"] } } } }
        Assert.assertEquals([customSrc], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([], plugin.sourceDirectorySetMap["instrumentTest"].files.toList().sort())
    }

    @Test
    public void updateCustomScalaInstrumentTestSourceSetToAndroidPlugin() {
        def plugin = getPlugin()
        def customSrc = new File(project.file("."), ["custom", "testSourceSet", "Src1Test.scala"].join(File.separator))
        customSrc.parentFile.mkdirs()
        customSrc.withWriter { it.write("class Src2Test{}") }
        project.android { sourceSets { instrumentTest { scala { srcDirs = ["custom/testSourceSet"] } } } }
        Assert.assertEquals([], plugin.sourceDirectorySetMap["main"].files.toList().sort())
        Assert.assertEquals([customSrc], plugin.sourceDirectorySetMap["instrumentTest"].files.toList().sort())
    }
}
