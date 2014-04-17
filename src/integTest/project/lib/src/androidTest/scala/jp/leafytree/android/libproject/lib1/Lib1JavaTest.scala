package jp.leafytree.android.libproject.lib1

import junit.framework.{TestCase, Assert}

class Lib1JavaTest extends TestCase {
  def testCallScalaClass() {
    Assert.assertEquals("Lib1Scala", new Lib1Scala().getName)
  }
}
