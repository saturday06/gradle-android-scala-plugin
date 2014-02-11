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

import com.google.common.io.ByteStreams
import org.apache.commons.lang.SystemUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DexTest {
    Project project
    File testDexJar
    File libraryDexJar
    Dex dex

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build()
        def dexToolsZip = new File([
                // TODO
                SystemUtils.USER_HOME,
                ".gradle", "caches", "modules-2", "files-2.1",
                "com.googlecode.dex2jar", "dex-tools", "0.0.9.15",
                "cc9366836d576ce22a18de8f214368636db9fcba",
                "dex-tools-0.0.9.15.zip"
        ].join(File.separator))
        def unzipDir = new File(project.buildDir, "temp")
        def dexToolsDir = new File([unzipDir.absolutePath, "dex2jar-0.0.9.15"].join(File.separator))
        project.ant.unzip(src: dexToolsZip, dest: unzipDir)

        dex = new Dex(project, dexToolsDir)
        def testDir = dex.mkdirsOrThrow("proguardTest")
        testDexJar = new File(testDir, "test.dex.jar")
        testDexJar.withOutputStream {
            ByteStreams.copy(getClass().getResourceAsStream("test.dex.jar"), it)
        }
        libraryDexJar = new File(testDir, "library.dex.jar")
        libraryDexJar.withOutputStream {
            ByteStreams.copy(getClass().getResourceAsStream("library.dex.jar"), it)
        }
    }

    @Test
    public void proguardKeepOnlyEmptyClass() {
        def config = """
        -dontoptimize
        -dontobfuscate
        -dontpreverify
        -dontwarn java.lang.**
        -keep class jp.leafytree.android.Empty { *; }
        """
        dex.proguard(testDexJar, [libraryDexJar], config)
        Assert.assertTrue(testDexJar.size() < 3000)
    }

    @Test
    public void proguardKeepHalfClasses() {
        def config = """
        -dontoptimize
        -dontobfuscate
        -dontpreverify
        -dontwarn java.lang.**
        -keep class jp.leafytree.android.Empty { *; }
        -keep class jp.leafytree.android.Data1 { *; }
        """
        dex.proguard(testDexJar, [libraryDexJar], config)
        Assert.assertTrue(testDexJar.size() > 15000)
        Assert.assertTrue(testDexJar.size() < 20000)
    }

    @Test
    public void proguardKeepAllClasses() {
        def config = """
        -dontoptimize
        -dontobfuscate
        -dontpreverify
        -dontwarn java.lang.**
        -keep class jp.leafytree.android.Empty { *; }
        -keep class jp.leafytree.android.Data1 { *; }
        -keep class jp.leafytree.android.Data2 { *; }
        """
        dex.proguard(testDexJar, [libraryDexJar], config)
        Assert.assertTrue(testDexJar.size() > 35000)
        Assert.assertTrue(testDexJar.size() < 40000)
    }

    @Test
    public void proguardThrowsExceptionIfDependenciesAreNotSet() {
        def config = """
        -dontoptimize
        -dontobfuscate
        -dontpreverify
        -dontwarn java.lang.**
        -keep class jp.leafytree.android.Empty { *; }
        -keep class jp.leafytree.android.Data1 { *; }
        -keep class jp.leafytree.android.Data2 { *; }
        """
        try {
            dex.proguard(testDexJar, [], config)
            Assert.fail("Should throw Exception")
        } catch (Exception e) {
            // ok
        }
    }
}
