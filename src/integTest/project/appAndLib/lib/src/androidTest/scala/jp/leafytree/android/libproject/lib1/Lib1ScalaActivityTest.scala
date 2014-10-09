package jp.leafytree.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert
import scala.io.Source

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity](classOf[Lib1ScalaActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Lib1Java", getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }

  def test3() {
    Assert.assertEquals(Source.fromString("x").toList(0), 'x')
  }
}
