package jp.leafytree.android.hello

import junit.framework.Assert
import org.junit.Test

import scala.collection.concurrent.TrieMap

class HelloScalaTest {

  @Test
  def simpleAssertion() {
    Assert.assertTrue(true)
  }

  @Test
  def simpleActivityAssertion() {
    Assert.assertEquals(3, new HelloScala().increment(2))
  }
}
