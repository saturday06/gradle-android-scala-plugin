package jp.leafytree.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import scala.collection.concurrent.TrieMap

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity_]("jp.leafytree.android.hello", classOf[HelloActivity_]) {
  def testSimpleAssertion() {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion() {
    Assert.assertEquals(new HelloJava().say + "\n" + new HelloScala().say, getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }

  def testCallScalaLibraryClassOfNotUsedByMainApp() {
    val map = new TrieMap[String, String]
    map.put("x", new HelloJava().say + "\n" + new HelloScala().say)
    Assert.assertEquals(map("x"), getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }
}
