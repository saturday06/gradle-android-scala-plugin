package jp.leafytree.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import android.view.ViewGroup
import android.widget.TextView
import jp.leafytree.android.libproject.R
import junit.framework.Assert
import jp.leafytree.android.libproject.lib1.{Lib1Java, Lib1JavaActivity}
import scala.io.Source

class Lib1JavaActivityTest extends ActivityInstrumentationTestCase2[Lib1JavaActivity](classOf[Lib1JavaActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(android.R.id.content).asInstanceOf[ViewGroup].getChildAt(0).asInstanceOf[TextView].getText)
  }

  def test3() {
    Assert.assertEquals(Source.fromString("x").toList(0), 'x')
  }
}
