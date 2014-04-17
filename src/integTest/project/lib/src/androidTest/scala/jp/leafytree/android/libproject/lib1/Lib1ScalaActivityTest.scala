package jp.leafytree.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import scala.collection.concurrent.TrieMap
import junit.framework.Assert

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity]("jp.leafytree.android.libproject.lib1", classOf[Lib1ScalaActivity]) {
  var solo: Solo = _

  override def setUp {
    solo = new Solo(getInstrumentation, getActivity)
  }

  def testSimpleAssertion {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion {
    solo.waitForText("Lib1Java")
  }

  def testCallScalaLibraryClassOfNotUsedByMainApp {
    val map = new TrieMap[String, String]
    map.put("x", "Lib1Java")
    solo.waitForText(map.apply("x"))
  }
}
