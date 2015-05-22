package jp.leafytree.android.libproject.lib1

import junit.framework.{TestCase, Assert}

class Lib1JavaTest extends TestCase {
  def test1() {
    Assert.assertEquals("Lib1Java", new Lib1Java().getName)
    Assert.assertEquals(List("Lib1Java").last, Option(new Lib1Java().getName).get)
  }
}
