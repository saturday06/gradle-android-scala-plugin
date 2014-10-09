package jp.leafytree.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.view.ViewGroup
import android.widget.TextView
import junit.framework.Assert
import scala.collection.concurrent.TrieMap

class Lib1JavaActivityTest extends ActivityInstrumentationTestCase2[Lib1JavaActivity](classOf[Lib1JavaActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(android.R.id.content).asInstanceOf[ViewGroup].getChildAt(0).asInstanceOf[TextView].getText)
  }

  def test3() {
    val map = TrieMap[String, String]()
    map.put("1", "Lib1Java")
    map.put("2", new Lib1Java().getName)
    Assert.assertEquals(map("1"), map("2"))
  }
}
