package jp.leafytree.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import scala.collection.concurrent.TrieMap

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity]("jp.leafytree.android.hello", classOf[HelloActivity]) {
  def testSimpleAssertion() {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion() {
    Assert.assertEquals("Hello. I'm Java !", getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }

  def testCallScalaLibraryClassOfNotUsedByMainApp() {
    val map = new TrieMap[String, String]
    map.put("x", "Hello. I'm Java !")
    Assert.assertEquals(map("x"), getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }
}
