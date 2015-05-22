package jp.leafytree.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import scala.collection.concurrent.TrieMap
import junit.framework.Assert

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity]("jp.leafytree.android.libproject.lib1", classOf[Lib1ScalaActivity]) {
  def testSimpleAssertion {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }

  def testCallScalaLibraryClassOfNotUsedByMainApp {
    val map = new TrieMap[String, String]
    map.put("x", "Lib1Java")
    Assert.assertEquals(map("x"), getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }
}
