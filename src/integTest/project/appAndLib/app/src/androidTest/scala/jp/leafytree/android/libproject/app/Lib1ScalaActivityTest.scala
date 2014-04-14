package jp.leafytree.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import junit.framework.Assert
import jp.leafytree.android.libproject.lib1.{Lib1Java, Lib1ScalaActivity}
import scala.collection.concurrent.TrieMap

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity](classOf[Lib1ScalaActivity]) {
  var solo: Solo = _

  override def setUp() {
    solo = new Solo(getInstrumentation(), getActivity())
  }

  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    solo.waitForText("Lib1Java")
  }

  def test3() {
    val map = TrieMap[String, String]()
    map.put("1", "Lib1Java")
    map.put("2", new Lib1Java().getName)
    Assert.assertEquals(map("1"), map("2"))
  }
}
