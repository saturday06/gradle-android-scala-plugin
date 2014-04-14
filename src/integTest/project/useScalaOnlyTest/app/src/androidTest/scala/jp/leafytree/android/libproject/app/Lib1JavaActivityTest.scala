package jp.leafytree.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import junit.framework.Assert
import jp.leafytree.android.libproject.lib1.{Lib1Java, Lib1JavaActivity}
import scala.io.Source

class Lib1JavaActivityTest extends ActivityInstrumentationTestCase2[Lib1JavaActivity](classOf[Lib1JavaActivity]) {
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
