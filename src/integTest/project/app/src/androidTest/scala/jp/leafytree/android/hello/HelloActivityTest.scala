package jp.leafytree.android.hello

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import junit.framework.Assert
import scala.collection.concurrent.TrieMap

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity]("jp.leafytree.android.hello", classOf[HelloActivity]) {
  var solo: Solo = _

  override def setUp() {
    solo = new Solo(getInstrumentation(), getActivity())
  }

  def testSimpleAssertion() {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion() {
    solo.waitForText("Hello. I'm Java !")
  }

  def testCallScalaLibraryClassOfNotUsedByMainApp() {
    val map = new TrieMap[String, String]
    map.put("x", "Hello. I'm Java !")
    solo.waitForText(map("x"))
  }
}
