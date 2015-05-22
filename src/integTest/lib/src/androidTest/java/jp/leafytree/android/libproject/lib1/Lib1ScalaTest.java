package jp.leafytree.android.libproject.lib1;

import junit.framework.TestCase;

public class Lib1ScalaTest extends TestCase {
    public void testCallScalaClass() {
        assertEquals("Lib1Scala", new Lib1Scala().getName());
    }
}

