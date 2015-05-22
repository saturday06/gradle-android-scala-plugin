package jp.leafytree.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import scala.collection.concurrent.TrieMap

class HelloScalaActivityTest extends ActivityInstrumentationTestCase2[HelloScalaActivity]("jp.leafytree.android.hello", classOf[HelloScalaActivity]) {
  var flavor: String = _

  override def setUp() {
    flavor = getInstrumentation().getTargetContext().getPackageName().replaceFirst(".*\\.", "")
  }

  def testSimpleAssertion() {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion() {
    Assert.assertEquals(f"${flavor}Java${flavor}Scala", getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }

  def testCallScalaLibraryClassOfNotUsedByMainApp() {
    val map = new TrieMap[String, String]
    map.put("x", f"${flavor}Java${flavor}Scala")
    Assert.assertEquals(map("x"), getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }
}
