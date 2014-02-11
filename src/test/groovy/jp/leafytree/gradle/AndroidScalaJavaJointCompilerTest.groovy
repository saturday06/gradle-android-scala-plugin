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
import org.gradle.api.Project
import org.gradle.api.internal.tasks.compile.DefaultJavaCompileSpec
import org.gradle.api.internal.tasks.compile.JavaCompileSpec
import org.gradle.api.tasks.compile.CompileOptions
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.testng.Assert

class AndroidScalaJavaJointCompilerTest {
    Project project

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    public void getScalacOptionsConvertsJavaOptionsToScala() {
        def compiler = new AndroidScalaJavaJointCompiler(project, null, [:])
        def spec = new DefaultJavaCompileSpec()
        def options = new CompileOptions()
        spec.setCompileOptions(options)

        options.setEncoding("UTF-8")
        Assert.assertEquals("UTF-8", compiler.getScalacOptions(spec)["encoding"])
        options.setEncoding("ISO-8859-1")
        Assert.assertEquals("ISO-8859-1", compiler.getScalacOptions(spec)["encoding"])
    }

    @Test
    public void getScalacOptionsRemovesJavaOnlyOptions() {
        Compiler<JavaCompileSpec> compileSpec
        def compiler = new AndroidScalaJavaJointCompiler(project, null, [:])
        def spec = new DefaultJavaCompileSpec()
        def options = new CompileOptions()
        spec.setCompileOptions(options)

        options.setDebug(true)
        Assert.assertFalse(compiler.getScalacOptions(spec).containsKey("debug"))
    }
}
