package jp.leafytree.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import junit.framework.Assert
import scala.io.Source

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
    Assert.assertEquals(Source.fromString("x").toList(0), 'x')
  }
}
