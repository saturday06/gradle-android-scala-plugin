package jp.leafytree.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import jp.leafytree.android.libproject.R
import junit.framework.Assert
import jp.leafytree.android.libproject.lib1.{Lib1Java, Lib1ScalaActivity}
import scala.collection.concurrent.TrieMap
import scalaz._
import Scalaz._

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity](classOf[Lib1ScalaActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }

  def test3() {
    val map = TrieMap[String, String]()
    map.put("1", "Lib1Java")
    map.put("2", new Lib1Java().getName)
    Assert.assertEquals(map("1"), map("2"))
  }

  def test4() {
    Assert.assertEquals(Success(123), "123".parseInt)
  }
}
