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

class GradleWrapper {
    private final File dir

    public GradleWrapper(File dir) {
        this.dir = dir
    }

    public Process execute(List<String> options) {
        def ext = (Os.isFamily(Os.FAMILY_WINDOWS) ? ".bat" : "")
        def gradlew = dir.absolutePath + File.separator + "gradlew" + ext
        def processBuilder = new ProcessBuilder([gradlew] + options)
        processBuilder.directory(dir)
        processBuilder.start()
    }
}
