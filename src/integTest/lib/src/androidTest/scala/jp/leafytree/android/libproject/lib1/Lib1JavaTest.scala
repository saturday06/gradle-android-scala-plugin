package jp.leafytree.android.libproject.lib1

import junit.framework.{TestCase, Assert}
import scalaz.Scalaz._

class Lib1JavaTest extends TestCase {
  def testCallScalaClass() {
    Assert.assertEquals("Lib1Scala", new Lib1Scala().getName)
  }

  def testScalazUsability() {
    Assert.assertEquals(12345, "12345".parseInt.getOrElse(0))
  }
}
