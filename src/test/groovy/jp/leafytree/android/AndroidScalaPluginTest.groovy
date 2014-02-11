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
}
