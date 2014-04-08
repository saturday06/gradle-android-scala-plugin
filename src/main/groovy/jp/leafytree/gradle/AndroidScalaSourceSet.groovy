package jp.leafytree.gradle

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.ScalaSourceSet
import org.gradle.util.ConfigureUtil

/**
 * Its original source were taken from official scala plugin:
 * <a href="https://github.com/gradle/gradle/blob/68df6c2e40232b6f9ea93b5ee825a50158baaeb6/subprojects/scala/src/main/groovy/org/gradle/api/internal/tasks/DefaultScalaSourceSet.java">
 *     DefaultScalaSourceSet.java
 * </a>
 *
 * A {@code ScalaSourceSetConvention} defines the properties and methods added to a {@link
 * org.gradle.api.tasks.SourceSet} by the {@code ScalaPlugin}.
 */
public class AndroidScalaSourceSet implements ScalaSourceSet {
    private final SourceDirectorySet scala;
    private final SourceDirectorySet allScala;

    public AndroidScalaSourceSet(String displayName, FileResolver fileResolver) {
        scala = new DefaultSourceDirectorySet(String.format("%s Scala source", displayName), fileResolver);
        scala.getFilter().include("**/*.java", "**/*.scala");
        allScala = new DefaultSourceDirectorySet(String.format("%s Scala source", displayName), fileResolver);
        allScala.getFilter().include("**/*.scala");
        allScala.source(scala);
    }

    /**
     * Returns the source to be compiled by the Scala compiler for this source set. This may contain both Java and Scala
     * source files.
     *
     * @return The Scala source. Never returns null.
     */
    public SourceDirectorySet getScala() {
        return scala;
    }

    /**
     * Configures the Scala source for this set.
     *
     * <p>The given closure is used to configure the {@link SourceDirectorySet} which contains the Scala source.
     *
     * @param configureClosure The closure to use to configure the Scala source.
     * @return this
     */
    public ScalaSourceSet scala(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getScala());
        return this;
    }

    /**
     * All Scala source for this source set.
     *
     * @return the Scala source. Never returns null.
     */
    public SourceDirectorySet getAllScala() {
        return allScala;
    }
}
